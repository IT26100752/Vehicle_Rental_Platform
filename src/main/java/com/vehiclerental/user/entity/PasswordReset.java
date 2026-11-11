package com.vehiclerental.user.entity;

import com.vehiclerental.common.BaseEntity;

import java.time.LocalDateTime;

/** A single-use "forgot password" link. Only the SHA-256 hash of the token is stored. */
public class PasswordReset extends BaseEntity {

    private Long customerId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;

    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired(LocalDateTime now) { return expiresAt == null || now.isAfter(expiresAt); }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
