package com.vehiclerental.payment.controller;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.util.SessionUtil;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.dto.PaymentRequest;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.user.entity.User;
import com.vehiclerental.vehicle.service.VehicleService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MEMBER 4 - Payment Management (SIMULATED payments: CARD / CASH / ONLINE).
 * Flow: booking details -> "Pay" button -> PENDING payment is created ->
 *       payment form -> confirm (COMPLETED) or cancel (FAILED).
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final VehicleService vehicleService;

    public PaymentController(PaymentService paymentService, BookingService bookingService,
                             VehicleService vehicleService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
    }

    /* ------------------------- CREATE ------------------------------- */
    @PostMapping("/create")
    public String create(@ModelAttribute PaymentRequest request,
                         HttpSession session,
                         RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        int bookingId = request.getBookingId();
        PaymentMethod method = request.getMethod();
        Booking booking = bookingService.findById(bookingId).orElse(null);
        if (booking == null || booking.getUserId() != user.getId()) {
            redirect.addFlashAttribute("error", "Booking not found");
            return "redirect:/bookings";
        }
        try {
            Payment payment = paymentService.createForBooking(bookingId, method);
            return "redirect:/payments/" + payment.getId();
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/" + bookingId;
        }
    }

    /* ------------------------- READ --------------------------------- */
    @GetMapping("/{id}")
    public String details(@PathVariable int id, HttpSession session, Model model,
                          RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        Payment payment = paymentService.findById(id).orElse(null);
        if (payment == null) {
            redirect.addFlashAttribute("error", "Payment not found");
            return "redirect:/payments";
        }
        Booking booking = bookingService.findById(payment.getBookingId()).orElse(null);
        if (booking == null || (booking.getUserId() != user.getId()
                && !(user instanceof AdminUser))) {
            redirect.addFlashAttribute("error", "This is not your payment");
            return "redirect:/payments";
        }
        model.addAttribute("payment", payment);
        model.addAttribute("booking", booking);
        vehicleService.findById(booking.getVehicleId())
                .ifPresent(v -> model.addAttribute("vehicle", v));
        if (payment.getStatus() == PaymentStatus.PENDING) {
            return "payment/payment";     // simulated checkout screen
        }
        return "payment/payment-details";      // receipt screen
    }

    @GetMapping
    public String history(HttpSession session, Model model) {
        User user = SessionUtil.currentUser(session);
        List<Integer> myBookingIds = bookingService.bookingsOf(user.getId())
                .stream().map(Booking::getId).toList();
        model.addAttribute("payments", paymentService.historyForBookings(myBookingIds));
        return "payment/payment-history";
    }

    /* ------------------------- UPDATE ------------------------------- */
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable int id, RedirectAttributes redirect) {
        paymentService.updateStatus(id, PaymentStatus.COMPLETED);
        redirect.addFlashAttribute("success", "Payment confirmed (simulated)");
        return "redirect:/payments/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable int id, RedirectAttributes redirect) {
        paymentService.updateStatus(id, PaymentStatus.FAILED);
        redirect.addFlashAttribute("success", "Payment cancelled");
        return "redirect:/payments/" + id;
    }

    /* ------------------------- ADMIN -------------------------------- */
    @GetMapping("/all")
    public String all(Model model) {
        model.addAttribute("payments", paymentService.history());
        model.addAttribute("revenue", paymentService.revenue());
        return "admin/payments";
    }
}
