package com.vehiclerental.vehicle.controller;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.vehicle.entity.Bike;
import com.vehiclerental.vehicle.entity.Car;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.Transmission;
import com.vehiclerental.vehicle.entity.Truck;
import com.vehiclerental.vehicle.entity.Van;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;
import com.vehiclerental.vehicle.dto.VehicleForm;
import com.vehiclerental.vehicle.service.VehicleService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MEMBER 2 - Vehicle Management (READ for everyone, CUD for admins).
 *
 * The interesting OOP bit is build(): the concrete subclass (Car / Van /
 * Bike / Truck) is chosen at RUNTIME from the "vehicleType" form parameter.
 * Jackson cannot do that automatically for an ABSTRACT class, so we do it
 * with a factory method - a viva talking point.
 */
@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ReviewService reviewService;
    private final BookingService bookingService;

    public VehicleController(VehicleService vehicleService, ReviewService reviewService,
                             BookingService bookingService) {
        this.vehicleService = vehicleService;
        this.reviewService = reviewService;
        this.bookingService = bookingService;
    }

    /* ---------------------------- READ ------------------------------ */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) VehicleType type,
                       Model model) {
        List<Vehicle> result = vehicleService.search(keyword, type);
        model.addAttribute("vehicles", result);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("types", VehicleType.values());
        return "vehicle/vehicles";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable int id, Model model, RedirectAttributes redirect) {
        return vehicleService.findById(id)
                .map(vehicle -> {
                    model.addAttribute("vehicle", vehicle);
                    model.addAttribute("reviews", reviewService.approvedForVehicle(id));
                    model.addAttribute("avgRating", reviewService.averageRating(id));
                    model.addAttribute("activeBooking",
                            bookingService.activeBookingForVehicle(id).orElse(null));
                    return "vehicle/vehicle-details";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Vehicle not found");
                    return "redirect:/vehicles";
                });
    }

    /* ---------------------------- CREATE ---------------------------- */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("fuels", FuelType.values());
        model.addAttribute("gears", Transmission.values());
        model.addAttribute("statuses", VehicleStatus.values());
        return "vehicle/vehicle-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute VehicleForm form, RedirectAttributes redirect) {
        String image = form.getImageUrl() == null || form.getImageUrl().isBlank()
                ? "/images/" + form.getVehicleType().name().toLowerCase() + ".svg"
                : form.getImageUrl();
        Vehicle vehicle = form.toVehicle(0, image);
        vehicleService.save(vehicle);
        redirect.addFlashAttribute("success", vehicle.getDisplayName() + " added");
        return "redirect:/vehicles";
    }

    /* ---------------------------- UPDATE ---------------------------- */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model, RedirectAttributes redirect) {
        return vehicleService.findById(id)
                .map(vehicle -> {
                    model.addAttribute("vehicle", vehicle);
                    model.addAttribute("types", VehicleType.values());
                    model.addAttribute("fuels", FuelType.values());
                    model.addAttribute("gears", Transmission.values());
                    model.addAttribute("statuses", VehicleStatus.values());
                    return "vehicle/vehicle-form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Vehicle not found");
                    return "redirect:/vehicles";
                });
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable int id, @ModelAttribute VehicleForm form,
                         RedirectAttributes redirect) {
        Vehicle existing = vehicleService.findById(id).orElse(null);
        if (existing == null) {
            redirect.addFlashAttribute("error", "Vehicle not found");
            return "redirect:/vehicles";
        }
        String image = form.getImageUrl() == null || form.getImageUrl().isBlank()
                ? existing.getImageUrl() : form.getImageUrl();
        Vehicle updated = form.toVehicle(id, image);
        vehicleService.update(updated);
        redirect.addFlashAttribute("success", updated.getDisplayName() + " updated");
        return "redirect:/vehicles/" + id;
    }

    /* ---------------------------- DELETE ---------------------------- */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirect) {
        boolean ok = vehicleService.delete(id);
        redirect.addFlashAttribute(ok ? "success" : "error",
                ok ? "Vehicle removed" : "Vehicle not found");
        return "redirect:/vehicles";
    }

    /* -------------------- quick status change ------------------------ */
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable int id, @RequestParam VehicleStatus status,
                               RedirectAttributes redirect) {
        vehicleService.setStatus(id, status);
        redirect.addFlashAttribute("success", "Status set to " + status.label());
        return "redirect:/vehicles/" + id;
    }
}
