package com.vehiclerental.admin.controller;

import com.vehiclerental.booking.dto.ReturnForm;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.payment.service.PaymentService;
import com.vehiclerental.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vehiclerental.common.DateRules;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Admin: monitor bookings, verify documents at pickup, process returns and fines. */
@Controller
public class AdminBookingController {

    private final BookingService bookings;
    private final PaymentService payments;
    private final UserService users;
    private final com.vehiclerental.driver.service.DriverService drivers;

    public AdminBookingController(BookingService bookings, PaymentService payments, UserService users,
                                  com.vehiclerental.driver.service.DriverService drivers) {
        this.drivers = drivers;
        this.bookings = bookings;
        this.payments = payments;
        this.users = users;
    }

    @GetMapping("/admin/bookings")
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) BookingStatus status, Model model) {
        model.addAttribute("bookings", bookings.search(q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("statuses", BookingStatus.values());
        return "admin/bookings";
    }

    @GetMapping("/admin/bookings/{id}")
    public String details(@PathVariable Long id, Model model) {
        Booking booking = bookings.findById(id);
        model.addAttribute("booking", booking);
        model.addAttribute("customer", users.findById(booking.getCustomerId()));
        model.addAttribute("payments", payments.forBooking(id));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("freeDrivers", booking.isWithDriver()
                ? drivers.findFree(booking.getPickupDate(), booking.getReturnDate()) : java.util.List.of());
        model.addAttribute("driverAssignment", drivers.liveAssignmentFor(id));
        model.addAttribute("preferredDriver", booking.getPreferredDriverId() == null ? null
                : drivers.findAll().stream().filter(d -> booking.getPreferredDriverId().equals(d.getId())).findFirst().orElse(null));
        if (!model.containsAttribute("returnForm")) {
            model.addAttribute("returnForm", new ReturnForm());
        }
        return "admin/booking-details";
    }

    /** JSON used by the return form: how late would it be on this date and what would the fine be? */
    @GetMapping("/admin/bookings/{id}/fine-preview")
    @ResponseBody
    public Map<String, Object> finePreview(@PathVariable Long id,
                                           @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Booking booking = bookings.findById(id);
        long lateDays = DateRules.lateDays(booking.getReturnDate(), date);
        BigDecimal fine = bookings.getLateFeePolicy().calculateFine(booking.getTotalAmount(), lateDays);
        return Map.of("lateDays", lateDays, "fine", fine, "total", booking.getTotalAmount().add(fine));
    }

    /** Pickup day: NIC + licence were checked against the booking, hand over the vehicle. */
    @PostMapping("/admin/bookings/{id}/activate")
    public String activate(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean verified,
                           HttpSession session, RedirectAttributes redirect) {
        try {
            bookings.activate(id, verified, Auth.name(session));
            Web.success(redirect, "Documents verified - booking is now Active and the vehicle is Rented.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }

    /** Return day: late days x 10% of the original amount becomes the fine. */
    @PostMapping("/admin/bookings/{id}/return")
    public String processReturn(@PathVariable Long id, @Valid @ModelAttribute("returnForm") ReturnForm form,
                                BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return details(id, model);
        }
        try {
            Booking booking = bookings.processReturn(id, form);
            if (booking.getStatus() == BookingStatus.FINE_DUE) {
                Web.success(redirect, "Returned " + booking.getLateDays() + " day(s) late. Fine of LKR "
                        + String.format("%,.2f", booking.getFineAmount()) + " added - collect it to complete the booking.");
            } else {
                Web.success(redirect, "Vehicle returned on time. Booking completed.");
            }
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }

    /** Cash received at the counter (rental or late fine). */
    @PostMapping("/admin/bookings/{id}/cash")
    public String cash(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            payments.recordCash(id, Auth.name(session));
            Web.success(redirect, "Cash payment recorded.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/admin/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            bookings.cancelByAdmin(id);
            Web.success(redirect, "Booking cancelled. Any rental payment was refunded.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/admin/bookings/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            bookings.delete(id);
            Web.success(redirect, "Booking record deleted.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
            return "redirect:/admin/bookings/" + id;
        }
        return "redirect:/admin/bookings";
    }
}
