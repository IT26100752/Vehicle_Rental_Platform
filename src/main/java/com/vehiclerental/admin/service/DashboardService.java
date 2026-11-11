package com.vehiclerental.admin.service;

import com.vehiclerental.admin.dto.DashboardStats;
import com.vehiclerental.admin.dto.RevenuePoint;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.service.VehicleService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Collects the numbers shown on the admin dashboard. */
@Service
public class DashboardService {

    private final UserService users;
    private final VehicleService vehicles;
    private final BookingService bookings;
    private final PaymentService payments;
    private final com.vehiclerental.complaint.service.ComplaintService complaints;
    private final com.vehiclerental.driver.service.DriverService drivers;
    private final com.vehiclerental.review.service.ReviewService reviews;

    public DashboardService(UserService users, VehicleService vehicles, BookingService bookings, PaymentService payments,
                            com.vehiclerental.complaint.service.ComplaintService complaints,
                            com.vehiclerental.driver.service.DriverService drivers,
                            com.vehiclerental.review.service.ReviewService reviews) {
        this.complaints = complaints;
        this.drivers = drivers;
        this.reviews = reviews;
        this.users = users;
        this.vehicles = vehicles;
        this.bookings = bookings;
        this.payments = payments;
    }

    public DashboardStats build() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalUsers(users.count());

        List<Vehicle> fleet = vehicles.findAll();
        stats.setTotalVehicles(fleet.size());
        long available = 0;
        long reserved = 0;
        long rented = 0;
        long maintenance = 0;
        for (Vehicle v : fleet) {
            VehicleStatus status = bookings.displayStatus(v);
            switch (status) {
                case AVAILABLE -> available++;
                case RESERVED -> reserved++;
                case RENTED -> rented++;
                case MAINTENANCE -> maintenance++;
            }
        }
        stats.setAvailableVehicles(available);
        stats.setReservedVehicles(reserved);
        stats.setRentedVehicles(rented);
        stats.setMaintenanceVehicles(maintenance);

        Map<BookingStatus, Long> counts = bookings.countByStatus();
        stats.setPendingBookings(counts.get(BookingStatus.PENDING_PAYMENT));
        stats.setPendingTransfers(payments.countPendingVerification());
        stats.setOpenComplaints(complaints.countOpen());
        stats.setUnassignedDrivers(drivers.countUnassigned());
        stats.setLowReviews(reviews.countLow());
        stats.setConfirmedBookings(counts.get(BookingStatus.CONFIRMED));
        stats.setActiveBookings(counts.get(BookingStatus.ACTIVE));
        stats.setFineDueBookings(counts.get(BookingStatus.FINE_DUE));
        stats.setCompletedBookings(counts.get(BookingStatus.COMPLETED));
        stats.setCancelledBookings(counts.get(BookingStatus.CANCELLED));

        LocalDate today = LocalDate.now();
        stats.setRevenueToday(payments.revenueOn(today));
        stats.setRevenueThisMonth(payments.revenueIn(YearMonth.from(today)));
        stats.setRevenueTotal(payments.totalRevenue());
        stats.setRevenueFromCompleted(payments.revenueFromCompletedRentals());

        List<String> dayLabels = new ArrayList<>();
        List<BigDecimal> dayValues = new ArrayList<>();
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE d", Locale.ENGLISH);
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            dayLabels.add(dayFormat.format(day));
            dayValues.add(payments.revenueOn(day));
        }
        stats.setDailyRevenue(toPoints(dayLabels, dayValues));

        List<String> monthLabels = new ArrayList<>();
        List<BigDecimal> monthValues = new ArrayList<>();
        DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(today).minusMonths(i);
            monthLabels.add(monthFormat.format(month));
            monthValues.add(payments.revenueIn(month));
        }
        stats.setMonthlyRevenue(toPoints(monthLabels, monthValues));

        stats.setMostRented(bookings.mostRented(5));
        return stats;
    }

    private static List<RevenuePoint> toPoints(List<String> labels, List<BigDecimal> values) {
        BigDecimal max = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        List<RevenuePoint> points = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            int percent = max.signum() == 0 ? 0
                    : Math.max(4, values.get(i).multiply(BigDecimal.valueOf(100)).divide(max, 0, java.math.RoundingMode.HALF_UP).intValue());
            if (values.get(i).signum() == 0) {
                percent = 0;
            }
            points.add(new RevenuePoint(labels.get(i), values.get(i), percent));
        }
        return points;
    }
}
