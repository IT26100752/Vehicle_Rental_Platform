package com.vehiclerental.booking.controller;

import com.vehiclerental.booking.dto.BookingRequest;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.util.SessionUtil;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.user.entity.User;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.vehicle.service.VehicleService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MEMBER 3 - Booking / Rental Management.
 */
@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final PaymentService paymentService;

    public BookingController(BookingService bookingService, VehicleService vehicleService,
                             UserService userService, PaymentService paymentService) {
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    /* ------------------------ my bookings --------------------------- */
    @GetMapping
    public String myBookings(HttpSession session, Model model) {
        User user = SessionUtil.currentUser(session);
        List<Booking> bookings = bookingService.bookingsOf(user.getId());
        decorate(bookings, model);
        model.addAttribute("bookings", bookings);
        return "booking/bookings";
    }

    /* ------------------------ booking form -------------------------- */
    @GetMapping("/new")
    public String newBooking(@RequestParam(required = false) Integer vehicleId,
                             HttpSession session, Model model) {
        model.addAttribute("vehicles", vehicleService.available());
        model.addAttribute("selectedVehicleId", vehicleId);
        model.addAttribute("today", LocalDate.now());
        return "booking/booking-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute BookingRequest request,
                         HttpSession session,
                         RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        int vehicleId = request.getVehicleId();
        try {
            Booking booking = bookingService.createBooking(user.getId(), vehicleId,
                    request.getPickupDate(), request.getReturnDate(),
                    request.getPickupLocation(), request.getReturnLocation());
            redirect.addFlashAttribute("success",
                    "Booking #" + booking.getId() + " created - total LKR "
                            + String.format("%,.2f", booking.getTotalAmount()));
            return "redirect:/bookings/" + booking.getId();
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/new?vehicleId=" + vehicleId;
        }
    }

    /* ------------------------ details ------------------------------- */
    @GetMapping("/{id}")
    public String details(@PathVariable int id, HttpSession session, Model model,
                          RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        Optional<Booking> found = bookingService.findById(id);
        if (found.isEmpty()) {
            redirect.addFlashAttribute("error", "Booking not found");
            return "redirect:/bookings";
        }
        Booking booking = found.get();
        boolean isAdmin = user instanceof com.vehiclerental.admin.entity.AdminUser;
        if (booking.getUserId() != user.getId() && !isAdmin) {
            redirect.addFlashAttribute("error", "This is not your booking");
            return "redirect:/bookings";
        }
        model.addAttribute("booking", booking);
        vehicleService.findById(booking.getVehicleId())
                .ifPresent(v -> model.addAttribute("vehicle", v));
        userService.findById(booking.getUserId())
                .ifPresent(u -> model.addAttribute("booker", u));
        paymentService.findByBookingId(booking.getId())
                .ifPresentOrElse(p -> model.addAttribute("payment", p),
                        () -> model.addAttribute("payment", null));
        return "booking/booking-details";
    }

    /* ------------------------ cancel / return ----------------------- */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        User user = SessionUtil.currentUser(session);
        Booking booking = bookingService.findById(id).orElse(null);
        if (booking == null || booking.getUserId() != user.getId()) {
            redirect.addFlashAttribute("error", "Booking not found");
            return "redirect:/bookings";
        }
        try {
            bookingService.cancelBooking(id);
            redirect.addFlashAttribute("success", "Booking #" + id + " cancelled");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/{id}/return")
    public String returnVehicle(@PathVariable int id, HttpSession session,
                                RedirectAttributes redirect) {
        try {
            Booking booking = bookingService.returnVehicle(id, LocalDate.now());
            String msg = booking.getFine() > 0
                    ? "Vehicle returned - late fine LKR " + String.format("%,.2f", booking.getFine())
                    : "Vehicle returned on time - no fine";
            redirect.addFlashAttribute("success", msg);
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/" + id;
    }

    /* ------------------------ admin view ---------------------------- */
    @GetMapping("/all")
    public String allBookings(Model model) {
        List<Booking> bookings = bookingService.findAll();
        decorate(bookings, model);
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    /** Adds vehicle names + customer names so templates stay simple. */
    private void decorate(List<Booking> bookings, Model model) {
        var vehicleNames = new java.util.HashMap<Integer, String>();
        var userNames = new java.util.HashMap<Integer, String>();
        for (Booking b : bookings) {
            vehicleNames.put(b.getId(), vehicleService.findById(b.getVehicleId())
                    .map(v -> v.getDisplayName()).orElse("Vehicle #" + b.getVehicleId()));
            userNames.put(b.getId(), userService.findById(b.getUserId())
                    .map(User::getFullName).orElse("User #" + b.getUserId()));
        }
        model.addAttribute("vehicleNames", vehicleNames);
        model.addAttribute("userNames", userNames);
    }
}
