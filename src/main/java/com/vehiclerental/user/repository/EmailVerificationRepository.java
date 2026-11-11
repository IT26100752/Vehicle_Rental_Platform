package com.vehiclerental.user.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.user.entity.EmailVerification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Stores verification codes (hashed) in data/verifications.txt */
@Repository
public class EmailVerificationRepository extends FileRepository<EmailVerification> {

    public EmailVerificationRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "verifications.txt"));
    }

    public List<EmailVerification> findByCustomerId(Long customerId) {
        return findWhere(v -> customerId.equals(v.getCustomerId()));
    }

    /** The newest code issued to this customer, used or not. */
    public Optional<EmailVerification> findLatest(Long customerId) {
        return findByCustomerId(customerId).stream().max(Comparator.comparing(EmailVerification::getCreatedAt));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|customerId|email|codeHash|expiresAt|attempts|usedAt|lastSentAt";
    }

    @Override
    protected String[] toFields(EmailVerification v) {
        return new String[]{text(v.getCustomerId()), text(v.getEmail()), text(v.getCodeHash()), text(v.getExpiresAt()),
                String.valueOf(v.getAttempts()), text(v.getUsedAt()), text(v.getLastSentAt())};
    }

    @Override
    protected EmailVerification fromFields(String[] f) {
        EmailVerification v = new EmailVerification();
        v.setCustomerId(lng(f, 0));
        v.setEmail(str(f, 1));
        v.setCodeHash(str(f, 2));
        v.setExpiresAt(dateTime(f, 3));
        v.setAttempts(integer(f, 4) == null ? 0 : integer(f, 4));
        v.setUsedAt(dateTime(f, 5));
        v.setLastSentAt(dateTime(f, 6));
        return v;
    }
}
