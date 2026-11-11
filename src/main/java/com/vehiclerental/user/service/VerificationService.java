package com.vehiclerental.user.service;

import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.notification.EmailMessage;
import com.vehiclerental.notification.EmailService;
import com.vehiclerental.notification.EmailTemplates;
import com.vehiclerental.notification.EmailType;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.EmailVerification;
import com.vehiclerental.user.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/** Email verification with a 6-digit one-time code: issue, resend (with cooldown), verify (expiry + attempt limit). */
@Service
public class VerificationService {

    private final EmailVerificationRepository repository;
    private final UserService users;
    private final EmailService email;
    private final EmailTemplates templates;
    private final PasswordEncoder encoder;
    private final int codeMinutes;
    private final int maxAttempts;
    private final int resendSeconds;
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(VerificationService.class.getName());
    private final SecureRandom random = new SecureRandom();
    public VerificationService(EmailVerificationRepository repository, UserService users, EmailService email,
                               EmailTemplates templates, PasswordEncoder encoder,
                               @Value("${app.verify.code-minutes:15}") int codeMinutes,
                               @Value("${app.verify.max-attempts:5}") int maxAttempts,
                               @Value("${app.verify.resend-seconds:60}") int resendSeconds) {
        this.repository = repository;
        this.users = users;
        this.email = email;
        this.templates = templates;
        this.encoder = encoder;
        this.codeMinutes = codeMinutes;
        this.maxAttempts = maxAttempts;
        this.resendSeconds = resendSeconds;
    }

    /** Creates a fresh code (old ones stop working) and emails it. */
    public void issue(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        for (EmailVerification old : repository.findByCustomerId(customer.getId())) {
            if (!old.isUsed()) {
                old.setUsedAt(now);
                repository.update(old);
            }
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        EmailVerification v = new EmailVerification();
        v.setCustomerId(customer.getId());
        v.setEmail(customer.getEmail());
        v.setCodeHash(encoder.encode(code));
        v.setExpiresAt(now.plusMinutes(codeMinutes));
        v.setLastSentAt(now);
        repository.create(v);
        if (!email.isDeliveringRealMail()) {
            // mail is saved to data/outbox only, so also print the code to the console for the developer
            LOG.info("Mail is not being sent. Verification code for " + customer.getEmail() + " is " + code);
        }

        EmailMessage message = templates.builder("Your verification code: " + code)
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("Use this code to verify your email address and finish creating your account.")
                .highlight("Verification code", code)
                .note("The code expires in " + codeMinutes + " minutes. If you did not create an account, ignore this email.")
                .build(customer.getEmail(), EmailType.VERIFICATION);
        email.send(message);
    }

    /** Resend with a cooldown. */
    public void resend(String emailAddress) {
        Customer customer = users.findByEmail(emailAddress);
        if (customer.isEmailVerified()) {
            throw new BusinessRuleException("This email address is already verified. You can sign in.");
        }
        long wait = secondsUntilResend(customer);
        if (wait > 0) {
            throw new BusinessRuleException("Please wait " + wait + " seconds before asking for a new code.");
        }
        issue(customer);
    }

    /** Checks the code; on success the account becomes verified. */
    public Customer verify(String emailAddress, String code) {
        Customer customer = users.findByEmail(emailAddress);
        if (customer.isEmailVerified()) {
            return customer;
        }
        String clean = code == null ? "" : code.replaceAll("\\s", "");
        if (!clean.matches("\\d{6}")) {
            throw new BusinessRuleException("Enter all 6 digits of the code.");
        }
        EmailVerification v = repository.findLatest(customer.getId()).filter(x -> !x.isUsed())
                .orElseThrow(() -> new BusinessRuleException("No active code. Press \"Send a new code\"."));
        if (v.isExpired(LocalDateTime.now())) {
            throw new BusinessRuleException("This code has expired. Press \"Send a new code\".");
        }
        if (v.getAttempts() >= maxAttempts) {
            throw new BusinessRuleException("Too many wrong attempts. Press \"Send a new code\".");
        }
        if (!encoder.matches(clean, v.getCodeHash())) {
            v.setAttempts(v.getAttempts() + 1);
            repository.update(v);
            int left = maxAttempts - v.getAttempts();
            throw new BusinessRuleException(left > 0 ? "That code is not correct. " + left + " attempt(s) left."
                    : "Too many wrong attempts. Press \"Send a new code\".");
        }
        v.setUsedAt(LocalDateTime.now());
        repository.update(v);
        return users.markEmailVerified(customer.getId());
    }

    public enum AccountState { PENDING, VERIFIED, UNKNOWN }

    public AccountState stateOf(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            return AccountState.UNKNOWN;
        }
        try {
            return users.findByEmail(emailAddress.trim().toLowerCase()).isEmailVerified()
                    ? AccountState.VERIFIED : AccountState.PENDING;
        } catch (RuntimeException notFound) {
            return AccountState.UNKNOWN;
        }
    }

    /** Is there an account with this address that still has to verify its email? */
    public boolean isPending(String emailAddress) {
        try {
            return emailAddress != null && !users.findByEmail(emailAddress.trim().toLowerCase()).isEmailVerified();
        } catch (RuntimeException notFound) {
            return false;
        }
    }

    public long secondsUntilResend(String emailAddress) {
        return secondsUntilResend(users.findByEmail(emailAddress.trim().toLowerCase()));
    }

    public long secondsUntilResend(Customer customer) {
        Optional<EmailVerification> latest = repository.findLatest(customer.getId());
        if (latest.isEmpty() || latest.get().getLastSentAt() == null) {
            return 0;
        }
        long passed = Duration.between(latest.get().getLastSentAt(), LocalDateTime.now()).getSeconds();
        return Math.max(0, resendSeconds - passed);
    }

    public int getCodeMinutes() {
        return codeMinutes;
    }
}
