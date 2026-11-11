package com.vehiclerental.user.entity;

import com.vehiclerental.common.BaseEntity;

import java.time.LocalDateTime;

/** One issued verification code. Only a BCrypt hash of the code is stored. */
public class EmailVerification extends BaseEntity {

    private Long customerId;
    private String email;
    private String codeHash;
    private LocalDateTime expiresAt;
    private int attempts;
    private LocalDateTime usedAt;
    private LocalDateTime lastSentAt;

    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired(LocalDateTime now) { return expiresAt == null || now.isAfter(expiresAt); }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
}
