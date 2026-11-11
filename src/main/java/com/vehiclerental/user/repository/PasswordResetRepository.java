package com.vehiclerental.user.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.user.entity.PasswordReset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Stores password reset links in data/password-resets.txt */
@Repository
public class PasswordResetRepository extends FileRepository<PasswordReset> {

    public PasswordResetRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "password-resets.txt"));
    }

    public Optional<PasswordReset> findByTokenHash(String hash) {
        return findFirst(r -> hash.equals(r.getTokenHash()));
    }

    public List<PasswordReset> findByCustomerId(Long customerId) {
        return findWhere(r -> customerId.equals(r.getCustomerId()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|customerId|tokenHash|expiresAt|usedAt";
    }

    @Override
    protected String[] toFields(PasswordReset r) {
        return new String[]{text(r.getCustomerId()), r.getTokenHash(), text(r.getExpiresAt()), text(r.getUsedAt())};
    }

    @Override
    protected PasswordReset fromFields(String[] f) {
        PasswordReset r = new PasswordReset();
        r.setCustomerId(lng(f, 0));
        r.setTokenHash(str(f, 1));
        r.setExpiresAt(dateTime(f, 2));
        r.setUsedAt(dateTime(f, 3));
        return r;
    }
}
