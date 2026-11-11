package com.vehiclerental.promo.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.promo.entity.PromoCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

/** Stores promo codes in data/promos.txt */
@Repository
public class PromoRepository extends FileRepository<PromoCode> {

    public PromoRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "promos.txt"));
    }

    public Optional<PromoCode> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        String wanted = code.trim().toUpperCase();
        return findFirst(p -> wanted.equals(p.getCode()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|code|description|percentOff|maxDiscount|validFrom|validTo|usageLimit|usedCount|premiumOnly|active";
    }

    @Override
    protected String[] toFields(PromoCode p) {
        return new String[]{p.getCode(), text(p.getDescription()), String.valueOf(p.getPercentOff()),
                p.getMaxDiscount().toPlainString(), text(p.getValidFrom()), text(p.getValidTo()),
                String.valueOf(p.getUsageLimit()), String.valueOf(p.getUsedCount()),
                String.valueOf(p.isPremiumOnly()), String.valueOf(p.isActive())};
    }

    @Override
    protected PromoCode fromFields(String[] f) {
        PromoCode p = new PromoCode();
        p.setCode(str(f, 0));
        p.setDescription(strOrNull(f, 1));
        p.setPercentOff(integer(f, 2) == null ? 0 : integer(f, 2));
        p.setMaxDiscount(str(f, 3).isEmpty() ? BigDecimal.ZERO : decimal(f, 3));
        p.setValidFrom(date(f, 4));
        p.setValidTo(date(f, 5));
        p.setUsageLimit(integer(f, 6) == null ? 0 : integer(f, 6));
        p.setUsedCount(integer(f, 7) == null ? 0 : integer(f, 7));
        p.setPremiumOnly(bool(f, 8));
        p.setActive(str(f, 9).isEmpty() || bool(f, 9));
        return p;
    }
}
