package com.vehiclerental.booking.pricing;

import com.vehiclerental.location.entity.Location;
import com.vehiclerental.user.entity.CustomerTier;
import com.vehiclerental.vehicle.entity.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the itemised price of a booking:
 * rental (strategy by customer tier) - discounts + driver fee + one-way fee.
 * Used by the live price preview, the booking form, the receipt and the admin pages, so they always agree.
 */
@Service
public class PricingService {

    private final PricingStrategy standard = new StandardPricing();
    private final PricingStrategy premium = new PremiumPricing();
    private final List<DiscountPolicy> discounts = List.of(new LongTermDiscount(), new WeekendDiscount());
    private final BigDecimal driverFeePerDay;
    private final BigDecimal oneWayFee;

    public PricingService(@Value("${app.driver.fee-per-day:3500}") BigDecimal driverFeePerDay,
                          @Value("${app.booking.one-way-fee:2500}") BigDecimal oneWayFee) {
        this.driverFeePerDay = driverFeePerDay.setScale(2, RoundingMode.HALF_UP);
        this.oneWayFee = oneWayFee.setScale(2, RoundingMode.HALF_UP);
    }

    public PricingStrategy strategyFor(CustomerTier tier) {
        return tier == CustomerTier.PREMIUM ? premium : standard;
    }

    public BigDecimal getDriverFeePerDay() {
        return driverFeePerDay;
    }

    public BigDecimal getOneWayFee() {
        return oneWayFee;
    }

    /** Pickup / drop locations may be null (the live preview before they are chosen). */
    public PriceQuote quote(Vehicle vehicle, CustomerTier tier, LocalDate pickup, long days, boolean withDriver,
                            Location pickupLocation, Location dropLocation) {
        List<PriceLine> lines = new ArrayList<>();
        PricingStrategy strategy = strategyFor(tier);

        BigDecimal listPrice = vehicle.calculateRent(days);
        BigDecimal rental = strategy.calculate(vehicle, days);
        lines.add(new PriceLine("Rental - " + days + (days == 1 ? " day" : " days") + " x LKR "
                + money(vehicle.getPricePerDay()), listPrice, "RENTAL"));

        BigDecimal discountTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal tierDiscount = listPrice.subtract(rental);
        if (tierDiscount.signum() > 0) {
            lines.add(new PriceLine("Premium member rate (" + (days >= 7 ? "15" : "10") + "% off)", tierDiscount.negate(), "DISCOUNT"));
            discountTotal = discountTotal.add(tierDiscount);
        }
        BigDecimal running = rental;
        for (DiscountPolicy policy : discounts) {
            BigDecimal d = policy.discountFor(running, days, pickup);
            if (d.signum() > 0) {
                lines.add(new PriceLine(policy.describe(), d.negate(), "DISCOUNT"));
                running = running.subtract(d);
                discountTotal = discountTotal.add(d);
            }
        }

        BigDecimal driverFee = BigDecimal.ZERO.setScale(2);
        if (withDriver && vehicle.isDriverAvailable()) {
            driverFee = driverFeePerDay.multiply(BigDecimal.valueOf(days));
            lines.add(new PriceLine("Driver - " + days + (days == 1 ? " day" : " days") + " x LKR "
                    + money(driverFeePerDay), driverFee, "DRIVER"));
        }

        BigDecimal oneWay = BigDecimal.ZERO.setScale(2);
        if (pickupLocation != null && dropLocation != null && !pickupLocation.getId().equals(dropLocation.getId())) {
            oneWay = oneWayFee;
            lines.add(new PriceLine("One-way fee (drop-off at a different location)", oneWay, "FEE"));
        }

        BigDecimal total = running.add(driverFee).add(oneWay).setScale(2, RoundingMode.HALF_UP);
        return new PriceQuote(days, lines, listPrice, discountTotal, driverFee, oneWay, total, strategy.describe());
    }

    private static String money(BigDecimal v) {
        return String.format("%,.2f", v);
    }
}
