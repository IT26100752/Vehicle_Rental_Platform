package com.vehiclerental.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Date arithmetic shared by the booking rules. */
public final class DateRules {

    private DateRules() {
    }

    /** Number of rental days: pickup -> return, never less than 1. */
    public static long rentalDays(LocalDate pickup, LocalDate returnDate) {
        return Math.max(1, ChronoUnit.DAYS.between(pickup, returnDate));
    }

    /**
     * Two periods clash when each starts before the other ends.
     * Boundaries are exclusive, so a car returned on the 10th can be picked up again on the 10th.
     */
    public static boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    /** Whole days the vehicle came back after the agreed return date (0 when on time or early). */
    public static long lateDays(LocalDate agreedReturn, LocalDate actualReturn) {
        return Math.max(0, ChronoUnit.DAYS.between(agreedReturn, actualReturn));
    }
}
