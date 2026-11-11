package com.vehiclerental.booking.controller;

import com.vehiclerental.booking.dto.BookingForm;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.location.service.LocationService;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.service.VehicleCatalogService;
import com.vehiclerental.vehicle.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Customer side of booking: details form, summary, Active Orders, history, cancel. */
@Controller
public class BookingController {

    private final BookingService bookings;
    private final VehicleService vehicles;
    private final VehicleCatalogService catalog;
    private final UserService users;
    private final PaymentService payments;
    private final ReviewService reviews;
    private final LocationService locations;
    private final com.vehiclerental.driver.service.DriverService drivers;

    public BookingController(BookingService bookings, VehicleService vehicles, VehicleCatalogService catalog,
                             UserService users, PaymentService payments, ReviewService reviews,
                             LocationService locations, com.vehiclerental.driver.service.DriverService drivers) {
        this.locations = locations;
        this.drivers = drivers;
        this.bookings = bookings;
        this.vehicles = vehicles;
        this.catalog = catalog;
        this.users = users;
        this.payments = payments;
        this.reviews = reviews;
    }

    /** Step 1: pick dates + enter renter details (pre-filled from the last rental). */
    @GetMapping("/bookings/new")
    public String form(@RequestParam("vehicleId") Long vehicleId,
                       @RequestParam(name = "pickupDate", required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
                       @RequestParam(name = "returnDate", required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                       @RequestParam(name = "withDriver", required = false) Boolean withDriver,
                       HttpSession session, Model model) {
        Vehicle vehicle = vehicles.findById(vehicleId);
        if (!model.containsAttribute("bookingForm")) {
            Customer customer = users.findById(Auth.userId(session));
            BookingForm form = new BookingForm();
            form.setVehicleId(vehicleId);
            form.setPickupDate(pickupDate);
            form.setReturnDate(returnDate);
            form.setRenterName(customer.getFullName());
            form.setNic(customer.getNic());
            form.setSex(customer.getSex());
            form.setAge(customer.getAge());
            form.setLicenceId(customer.getLicenceId());
            form.setWithDriver(Boolean.TRUE.equals(withDriver) && vehicle.isDriverAvailable());
            model.addAttribute("bookingForm", form);
        }
        model.addAttribute("card", catalog.card(vehicle));
        model.addAttribute("tier", users.findById(Auth.userId(session)).getTier());
        formLists(model);
        return "booking/booking-form";
    }

    /** Step 2: check availability, create the (unpaid) booking and show the summary. */
    @PostMapping("/bookings")
    public String create(@Valid @ModelAttribute("bookingForm") BookingForm form, BindingResult result,
                         HttpSession session, Model model) {
        Vehicle vehicle = vehicles.findById(form.getVehicleId());
        if (!result.hasErrors()) {
            try {
                Booking booking = bookings.createBooking(Auth.userId(session), form);
                return "redirect:/bookings/" + booking.getId() + "/summary";
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        model.addAttribute("card", catalog.card(vehicle));
        model.addAttribute("tier", users.findById(Auth.userId(session)).getTier());
        formLists(model);
        return "booking/booking-form";
    }

    /** Dropdown data for the booking form: active locations, pickup times, driver / one-way fees. */
    private void formLists(Model model) {
        model.addAttribute("locations", locations.findActive());
        java.util.List<String> times = new java.util.ArrayList<>();
        for (int h = 5; h <= 22; h++) {
            times.add(String.format("%02d:00", h));
            times.add(String.format("%02d:30", h));
        }
        model.addAttribute("pickupTimes", times);
        model.addAttribute("driverChoices", drivers.findAll().stream()
                .filter(com.vehiclerental.driver.entity.Driver::isAssignable).toList());
        model.addAttribute("driverFee", bookings.getPricing().getDriverFeePerDay());
        model.addAttribute("oneWayFee", bookings.getPricing().getOneWayFee());
    }

    /** Step 3: booking summary with the total, before payment. */
    @GetMapping("/bookings/{id}/summary")
    public String summary(@PathVariable Long id, HttpSession session, Model model) {
        Booking booking = bookings.findForCustomer(id, Auth.userId(session));
        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicles.findById(booking.getVehicleId()));
        return "booking/booking-summary";
    }

    /** "Active Orders": all open bookings. */
    @GetMapping("/bookings")
    public String activeOrders(HttpSession session, Model model) {
        model.addAttribute("orders", bookings.activeOrders(Auth.userId(session)));
        return "booking/bookings";
    }

    /** Completed and cancelled bookings, with the review button. */
    @GetMapping("/bookings/history")
    public String history(HttpSession session, Model model) {
        Long customerId = Auth.userId(session);
        java.util.List<Booking> history = bookings.history(customerId);
        model.addAttribute("history", history);
        model.addAttribute("reviewedIds", history.stream()
                .filter(b -> reviews.existsForBooking(b.getId())).map(Booking::getId).toList());
        return "booking/booking-history";
    }

    @GetMapping("/bookings/{id}")
    public String details(@PathVariable Long id, HttpSession session, Model model) {
        Booking booking = bookings.findForCustomer(id, Auth.userId(session));
        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments.forBooking(id));
        model.addAttribute("reviewed", reviews.existsForBooking(id));
        model.addAttribute("canCancel", booking.isCancellableByCustomer(LocalDate.now()));
        model.addAttribute("tier", users.findById(Auth.userId(session)).getTier());
        return "booking/booking-details";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            bookings.cancelByCustomer(id, Auth.userId(session));
            Web.success(redirect, "Booking cancelled." );
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/bookings";
    }
}
