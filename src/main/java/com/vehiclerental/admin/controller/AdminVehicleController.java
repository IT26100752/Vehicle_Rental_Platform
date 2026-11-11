package com.vehiclerental.admin.controller;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.vehicle.dto.VehicleFilter;
import com.vehiclerental.vehicle.dto.VehicleForm;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;
import com.vehiclerental.vehicle.service.VehicleCatalogService;
import com.vehiclerental.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin: fleet management (add, view, update, change status, delete). */
@Controller
public class AdminVehicleController {

    private final VehicleService vehicles;
    private final VehicleCatalogService catalog;
    private final BookingService bookings;

    public AdminVehicleController(VehicleService vehicles, VehicleCatalogService catalog, BookingService bookings) {
        this.vehicles = vehicles;
        this.catalog = catalog;
        this.bookings = bookings;
    }

    @GetMapping("/admin/vehicles")
    public String list(@ModelAttribute("filter") VehicleFilter filter, Model model) {
        filter.setPickupDate(null);
        filter.setReturnDate(null);
        model.addAttribute("cards", catalog.search(filter));
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("fuels", FuelType.values());
        model.addAttribute("statuses", VehicleStatus.values());
        return "admin/vehicles";
    }

    @GetMapping("/admin/vehicles/new")
    public String newForm(Model model) {
        model.addAttribute("vehicleForm", new VehicleForm());
        lists(model, false);
        return "vehicle/vehicle-form";
    }

    @PostMapping("/admin/vehicles")
    public String create(@Valid @ModelAttribute("vehicleForm") VehicleForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                vehicles.create(form);
                Web.success(redirect, "Vehicle added.");
                return "redirect:/admin/vehicles";
            } catch (BusinessRuleException ex) {
                result.rejectValue("registrationNo", "rule", ex.getMessage());
            }
        }
        lists(model, false);
        return "vehicle/vehicle-form";
    }

    @GetMapping("/admin/vehicles/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("vehicleForm", VehicleForm.from(vehicles.findById(id)));
        lists(model, true);
        return "vehicle/vehicle-form";
    }

    @PostMapping("/admin/vehicles/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("vehicleForm") VehicleForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                vehicles.update(id, form);
                Web.success(redirect, "Vehicle updated.");
                return "redirect:/admin/vehicles";
            } catch (BusinessRuleException ex) {
                result.rejectValue("registrationNo", "rule", ex.getMessage());
            }
        }
        lists(model, true);
        return "vehicle/vehicle-form";
    }

    @PostMapping("/admin/vehicles/{id}/status")
    public String status(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam VehicleStatus status,
                         RedirectAttributes redirect) {
        if (status == VehicleStatus.RESERVED) {
            Web.error(redirect, "Reserved is set automatically from bookings.");
        } else {
            vehicles.updateStatus(id, status);
            Web.success(redirect, "Vehicle status updated.");
        }
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/admin/vehicles/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        if (bookings.hasOpenBookings(id)) {
            Web.error(redirect, "This vehicle has bookings that are still open. Cancel or complete them first.");
        } else {
            vehicles.delete(id);
            Web.success(redirect, "Vehicle removed.");
        }
        return "redirect:/admin/vehicles";
    }

    private void lists(Model model, boolean editing) {
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("fuels", FuelType.values());
        model.addAttribute("statuses", VehicleStatus.values());
        model.addAttribute("images", VehicleService.IMAGE_PRESETS);
        model.addAttribute("transmissions", com.vehiclerental.vehicle.entity.Transmission.values());
        model.addAttribute("editing", editing);
    }
}
