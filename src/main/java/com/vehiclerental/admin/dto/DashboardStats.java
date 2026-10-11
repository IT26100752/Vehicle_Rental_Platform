package com.vehiclerental.admin.dto;

import com.vehiclerental.booking.entity.Booking;

import java.util.List;
import java.util.Map;

/** Everything the admin dashboard template needs, in one immutable object. */
public record DashboardStats(long userCount,
                             long vehicleCount,
                             long bookingCount,
                             double revenue,
                             long activeBookings,
                             long pendingReviews,
                             Map<String, Long> fleetByType,
                             List<Booking> recentBookings) {
}
