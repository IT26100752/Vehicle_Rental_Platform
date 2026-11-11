package com.vehiclerental.driver.controller;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.driver.dto.DriverForm;
import com.vehiclerental.driver.entity.DriverStatus;
import com.vehiclerental.driver.service.DriverService;
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

/** Admin: the driver roster, and assigning a driver to a booking. */
@Controller
public class AdminDriverController {

    private final DriverService drivers;
    private final BookingService bookings;

    public AdminDriverController(DriverService drivers, BookingService bookings) {
        this.drivers = drivers;
        this.bookings = bookings;
    }

    @GetMapping("/admin/drivers")
    public String list(Model model) {
        page(model, new DriverForm(), false);
        return "admin/drivers";
    }

    @GetMapping("/admin/drivers/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        page(model, DriverForm.from(drivers.findById(id)), true);
        return "admin/drivers";
    }

    @PostMapping("/admin/drivers")
    public String create(@Valid @ModelAttribute("driverForm") DriverForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            page(model, form, false);
            return "admin/drivers";
        }
        drivers.create(form);
        Web.success(redirect, "Driver added.");
        return "redirect:/admin/drivers";
    }

    @PostMapping("/admin/drivers/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("driverForm") DriverForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            page(model, form, true);
            return "admin/drivers";
        }
        drivers.update(id, form);
        Web.success(redirect, "Driver updated.");
        return "redirect:/admin/drivers";
    }

    @PostMapping("/admin/drivers/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            drivers.delete(id);
            Web.success(redirect, "Driver removed.");
        } catch (RuntimeException e) {
            Web.error(redirect, e.getMessage());
        }
        return "redirect:/admin/drivers";
    }

    /** From the booking page: pick a driver for a "with driver" booking. */
    @PostMapping("/admin/bookings/{bookingId}/driver")
    public String assign(@PathVariable Long bookingId, @RequestParam("driverId") Long driverId,
                         RedirectAttributes redirect) {
        try {
            drivers.assign(bookingId, driverId);
            Web.success(redirect, "Driver assigned and notified.");
        } catch (RuntimeException e) {
            Web.error(redirect, e.getMessage());
        }
        return "redirect:/admin/bookings/" + bookingId;
    }

    private void page(Model model, DriverForm form, boolean editing) {
        model.addAttribute("drivers", drivers.findAll());
        model.addAttribute("driverForm", form);
        model.addAttribute("editing", editing);
        model.addAttribute("statuses", DriverStatus.values());
    }
}
