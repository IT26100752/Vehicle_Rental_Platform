package com.vehiclerental.user.controller;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.config.Auth;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.vehicle.service.VehicleCatalogService;
import com.vehiclerental.vehicle.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/** Landing page and the customer dashboard. */
@Controller
public class HomeController {

    private final VehicleCatalogService catalog;
    private final VehicleService vehicles;
    private final ReviewService reviews;
    private final BookingService bookings;
    private final PaymentService payments;
    private final UserService users;
    private final com.vehiclerental.location.service.LocationService locations;
    private final com.vehiclerental.promo.service.PromoService promos;

    public HomeController(VehicleCatalogService catalog, VehicleService vehicles, ReviewService reviews,
                          BookingService bookings, PaymentService payments, UserService users,
                          com.vehiclerental.location.service.LocationService locations,
                          com.vehiclerental.promo.service.PromoService promos) {
        this.promos = promos;
        this.catalog = catalog;
        this.vehicles = vehicles;
        this.reviews = reviews;
        this.bookings = bookings;
        this.payments = payments;
        this.users = users;
        this.locations = locations;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featured", catalog.featured(6));
        model.addAttribute("fleetSize", vehicles.count());
        model.addAttribute("pins", locations.mapPins());
        model.addAttribute("latestReviews", reviews.visible().stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Customer customer = users.findById(Auth.userId(session));
        List<Booking> active = bookings.activeOrders(customer.getId());
        List<Booking> history = bookings.history(customer.getId());
        model.addAttribute("customer", customer);
        model.addAttribute("activeOrders", active.stream().limit(3).toList());
        model.addAttribute("activeCount", active.size());
        model.addAttribute("completedCount", history.stream()
                .filter(b -> b.getStatus() == com.vehiclerental.booking.entity.BookingStatus.COMPLETED).count());
        model.addAttribute("totalSpent", payments.forCustomer(customer.getId()).stream()
                .filter(p -> p.getStatus() == com.vehiclerental.payment.entity.PaymentStatus.PAID)
                .map(com.vehiclerental.payment.entity.Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("featured", catalog.featured(4));
        boolean premium = customer.getTier() == com.vehiclerental.user.entity.CustomerTier.PREMIUM;
        long counted = bookings.membershipBookingCount(customer.getId());
        int threshold = bookings.getPremiumThreshold();
        model.addAttribute("premium", premium);
        model.addAttribute("membershipCount", counted);
        model.addAttribute("premiumThreshold", threshold);
        model.addAttribute("bookingsToPremium", Math.max(0, threshold + 1 - counted));
        model.addAttribute("premiumProgress", Math.min(100, counted * 100 / (threshold + 1)));
        model.addAttribute("offers", promos.findAll().stream()
                .filter(p -> p.problemFor(premium, java.time.LocalDate.now()) == null).limit(4).toList());
        return "user/dashboard";
    }
}
