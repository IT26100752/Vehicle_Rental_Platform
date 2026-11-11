package com.vehiclerental.promo.service;

import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.promo.entity.PromoCode;
import com.vehiclerental.promo.repository.PromoRepository;
import com.vehiclerental.user.entity.CustomerTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Promo codes: admin CRUD plus the check and the discount calculation used when a customer books. */
@Service
public class PromoService {

    private final PromoRepository repository;

    public PromoService(PromoRepository repository) {
        this.repository = repository;
    }

    // ------------------------------------------------------------ customer side

    /** The code if the customer may use it today; otherwise a BusinessRuleException with the reason. */
    public PromoCode validate(String code, CustomerTier tier) {
        PromoCode promo = repository.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException("That promo code does not exist"));
        String problem = promo.problemFor(tier == CustomerTier.PREMIUM, LocalDate.now());
        if (problem != null) {
            throw new BusinessRuleException(problem);
        }
        return promo;
    }

    /** Discount in LKR on the given amount (the rental part), never more than the amount itself. */
    public BigDecimal discountFor(PromoCode promo, BigDecimal amount) {
        BigDecimal d = amount.multiply(BigDecimal.valueOf(promo.getPercentOff()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (promo.getMaxDiscount().signum() > 0 && d.compareTo(promo.getMaxDiscount()) > 0) {
            d = promo.getMaxDiscount().setScale(2, RoundingMode.HALF_UP);
        }
        return d.min(amount);
    }

    public void recordUse(PromoCode promo) {
        promo.setUsedCount(promo.getUsedCount() + 1);
        repository.update(promo);
    }

    // --------------------------------------------------------------- admin side

    public List<PromoCode> findAll() {
        return repository.findAll().stream().sorted(Comparator.comparing(PromoCode::getId).reversed()).toList();
    }

    public PromoCode findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessRuleException("That promo code no longer exists"));
    }

    public long count() {
        return repository.count();
    }

    public PromoCode save(Long id, String code, String description, int percentOff, BigDecimal maxDiscount,
                          LocalDate validFrom, LocalDate validTo, int usageLimit, boolean premiumOnly, boolean active) {
        String clean = code == null ? "" : code.trim().toUpperCase();
        if (!clean.matches("[A-Z0-9_-]{3,20}")) {
            throw new BusinessRuleException("The code must be 3 - 20 letters or digits (no spaces)");
        }
        if (percentOff < 1 || percentOff > 90) {
            throw new BusinessRuleException("The discount must be between 1% and 90%");
        }
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new BusinessRuleException("The end date cannot be before the start date");
        }
        if (usageLimit < 0 || (maxDiscount != null && maxDiscount.signum() < 0)) {
            throw new BusinessRuleException("Limits cannot be negative");
        }
        PromoCode other = repository.findByCode(clean).orElse(null);
        if (other != null && !other.getId().equals(id)) {
            throw new BusinessRuleException("The code " + clean + " already exists");
        }
        PromoCode promo = id == null ? new PromoCode() : findById(id);
        promo.setCode(clean);
        promo.setDescription(description == null || description.isBlank() ? null : description.trim());
        promo.setPercentOff(percentOff);
        promo.setMaxDiscount(maxDiscount);
        promo.setValidFrom(validFrom);
        promo.setValidTo(validTo);
        promo.setUsageLimit(usageLimit);
        promo.setPremiumOnly(premiumOnly);
        promo.setActive(active);
        return id == null ? repository.create(promo) : repository.update(promo);
    }

    public void setActive(Long id, boolean active) {
        PromoCode promo = findById(id);
        promo.setActive(active);
        repository.update(promo);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
