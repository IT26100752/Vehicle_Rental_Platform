package com.vehiclerental.admin.service;

import com.vehiclerental.admin.dto.DashboardStats;
import com.vehiclerental.admin.repository.AdminRepository;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.user.entity.User;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.vehicle.service.VehicleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Admin Management service.
 *
 * The admin module does NOT duplicate the other classes. It RE-USES
 * UserService / VehicleService / BookingService / PaymentService /
 * ReviewService and only combines their results into dashboard statistics.
 */
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;

    public AdminService(AdminRepository adminRepository, UserService userService,
                        VehicleService vehicleService, BookingService bookingService,
                        PaymentService paymentService, ReviewService reviewService) {
        this.adminRepository = adminRepository;
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
    }

    /** One object that carries everything the dashboard template needs. */
    public DashboardStats dashboardStats() {
        return new DashboardStats(
                userService.count(),
                vehicleService.count(),
                bookingService.count(),
                paymentService.revenue(),
                bookingService.active().size(),
                reviewService.pending().size(),
                vehicleService.fleetSummaryByType(),
                bookingService.recent(5));
    }

    public List<User> admins() {
        return adminRepository.findAllAdmins();
    }
}
