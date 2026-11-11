package com.vehiclerental.user.service;

import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.notification.EmailService;
import com.vehiclerental.notification.EmailTemplates;
import com.vehiclerental.notification.EmailType;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.PasswordReset;
import com.vehiclerental.user.repository.PasswordResetRepository;
import com.vehiclerental.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * "Forgot password": the customer asks for a link by email, the link carries a random single-use token
 * (valid for a short time) and opens a page where a new password is chosen.
 * The page never reveals whether an email address has an account.
 */
@Service
public class PasswordResetService {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PasswordResetService.class.getName());

    private final PasswordResetRepository repository;
    private final UserRepository users;
    private final EmailService email;
    private final EmailTemplates templates;
    private final PasswordEncoder encoder;
    private final int linkMinutes;
    private final int cooldownSeconds;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetRepository repository, UserRepository users, EmailService email,
                                EmailTemplates templates, PasswordEncoder encoder,
                                @Value("${app.reset.link-minutes:30}") int linkMinutes,
                                @Value("${app.reset.cooldown-seconds:60}") int cooldownSeconds) {
        this.repository = repository;
        this.users = users;
        this.email = email;
        this.templates = templates;
        this.encoder = encoder;
        this.linkMinutes = linkMinutes;
        this.cooldownSeconds = cooldownSeconds;
    }

    /** Sends the link if the address belongs to a verified, active account; otherwise does nothing (silently). */
    public void request(String emailAddress) {
        Optional<Customer> found = users.findByEmail(emailAddress);
        if (found.isEmpty() || !found.get().isEmailVerified() || !found.get().isLoginAllowed()) {
            return;
        }
        Customer customer = found.get();
        LocalDateTime now = LocalDateTime.now();
        for (PasswordReset old : repository.findByCustomerId(customer.getId())) {
            if (!old.isUsed() && old.getCreatedAt() != null
                    && Duration.between(old.getCreatedAt(), now).getSeconds() < cooldownSeconds) {
                return;                                    // asked a moment ago: do not send another mail
            }
        }
        for (PasswordReset old : repository.findByCustomerId(customer.getId())) {
            if (!old.isUsed()) {                           // earlier links stop working
                old.setUsedAt(now);
                repository.update(old);
            }
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        PasswordReset reset = new PasswordReset();
        reset.setCustomerId(customer.getId());
        reset.setTokenHash(hash(token));
        reset.setExpiresAt(now.plusMinutes(linkMinutes));
        repository.create(reset);

        String path = "/reset-password?token=" + token;
        if (!email.isDeliveringRealMail()) {
            LOG.info("Mail is not being sent. Password reset link for " + customer.getEmail() + ": " + templates.getBaseUrl() + path);
        }
        email.send(templates.builder("Reset your password")
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("We received a request to reset the password of your account. Press the button to choose a new one.")
                .button("Choose a new password", path)
                .note("The link works once and expires in " + linkMinutes + " minutes. If you did not ask for this, ignore this email; your password stays the same.")
                .build(customer.getEmail(), EmailType.GENERAL));
    }

    /** True when the link can still be used. */
    public boolean isValid(String token) {
        return find(token).isPresent();
    }

    public void reset(String token, String password, String confirm) {
        PasswordReset reset = find(token).orElseThrow(() ->
                new BusinessRuleException("This reset link is invalid or has expired. Please ask for a new one."));
        if (password == null || password.length() < 8) {
            throw new BusinessRuleException("The password must be at least 8 characters");
        }
        if (!password.equals(confirm)) {
            throw new BusinessRuleException("The two passwords do not match");
        }
        Customer customer = users.findById(reset.getCustomerId())
                .orElseThrow(() -> new BusinessRuleException("This account no longer exists"));
        customer.setPasswordHash(encoder.encode(password));
        users.update(customer);
        reset.setUsedAt(LocalDateTime.now());
        repository.update(reset);
        email.send(templates.builder("Your password was changed")
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("The password of your account was just changed. If this was not you, please contact us straight away.")
                .build(customer.getEmail(), EmailType.GENERAL));
    }

    private Optional<PasswordReset> find(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHash(hash(token.trim()))
                .filter(r -> !r.isUsed() && !r.isExpired(LocalDateTime.now()));
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public int getLinkMinutes() {
        return linkMinutes;
    }
}
