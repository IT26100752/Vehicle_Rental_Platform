package com.vehiclerental.promo.entity;

import com.vehiclerental.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A discount code the customer types on the booking form. Managed by the admin. */
public class PromoCode extends BaseEntity {

    private String code;
    private String description;
    /** Percentage taken off the rental part of the price, e.g. 10 for 10%. */
    private int percentOff;
    /** Upper limit of the discount in LKR; zero means no limit. */
    private BigDecimal maxDiscount = BigDecimal.ZERO;
    private LocalDate validFrom;
    private LocalDate validTo;
    /** How many times it can be used in total; zero means unlimited. */
    private int usageLimit;
    private int usedCount;
    private boolean premiumOnly;
    private boolean active = true;

    /** Why the code cannot be used right now, or null when it can. */
    public String problemFor(boolean premiumCustomer, LocalDate today) {
        if (!active) return "This promo code is not active";
        if (premiumOnly && !premiumCustomer) return "This promo code is for premium members only";
        if (validFrom != null && today.isBefore(validFrom)) return "This promo code is not valid yet";
        if (validTo != null && today.isAfter(validTo)) return "This promo code has expired";
        if (usageLimit > 0 && usedCount >= usageLimit) return "This promo code has been fully used";
        return null;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code == null ? null : code.trim().toUpperCase(); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPercentOff() { return percentOff; }
    public void setPercentOff(int percentOff) { this.percentOff = percentOff; }
    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount == null ? BigDecimal.ZERO : maxDiscount; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public boolean isPremiumOnly() { return premiumOnly; }
    public void setPremiumOnly(boolean premiumOnly) { this.premiumOnly = premiumOnly; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
