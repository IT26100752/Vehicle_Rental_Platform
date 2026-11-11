package com.vehiclerental.vehicle.controller;

import com.vehiclerental.booking.dto.Availability;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.vehicle.dto.VehicleFilter;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleType;
import com.vehiclerental.vehicle.service.VehicleCatalogService;
import com.vehiclerental.vehicle.service.VehicleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

/** Public vehicle catalogue: browse, search, filter, details and the availability check. */
@Controller
public class VehicleController {

    private final VehicleService vehicles;
    private final VehicleCatalogService catalog;
    private final BookingService bookings;
    private final ReviewService reviews;
    private final com.vehiclerental.user.service.UserService users;

    public VehicleController(VehicleService vehicles, VehicleCatalogService catalog,
                             BookingService bookings, ReviewService reviews,
                             com.vehiclerental.user.service.UserService users) {
        this.users = users;
        this.vehicles = vehicles;
        this.catalog = catalog;
        this.bookings = bookings;
        this.reviews = reviews;
    }

    @GetMapping("/vehicles")
    public String list(@ModelAttribute("filter") VehicleFilter filter, Model model) {
        model.addAttribute("cards", catalog.search(filter));
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("fuels", FuelType.values());
        return "vehicle/vehicles";
    }

    @GetMapping("/vehicles/{id}")
    public String details(@PathVariable Long id,
                          @RequestParam(name = "pickupDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
                          @RequestParam(name = "returnDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                          Model model) {
        Vehicle vehicle = vehicles.findById(id);
        model.addAttribute("card", catalog.card(vehicle));
        model.addAttribute("reviewList", reviews.visibleForVehicle(id));
        model.addAttribute("pickupDate", pickupDate);
        model.addAttribute("returnDate", returnDate);
        return "vehicle/vehicle-details";
    }

    /** Used by the date picker (JSON): is this vehicle free for these dates, and what would it cost? */
    @GetMapping("/api/vehicles/{id}/availability")
    @ResponseBody
    public Availability availability(@PathVariable Long id,
                                     @RequestParam(name = "pickup", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickup,
                                     @RequestParam(name = "ret", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                     @RequestParam(name = "withDriver", required = false) Boolean withDriver,
                                     @RequestParam(name = "pickupLocationId", required = false) Long pickupLocationId,
                                     @RequestParam(name = "dropLocationId", required = false) Long dropLocationId,
                                     @RequestParam(name = "promo", required = false) String promo,
                                     jakarta.servlet.http.HttpSession session) {
        com.vehiclerental.user.entity.CustomerTier tier = com.vehiclerental.user.entity.CustomerTier.STANDARD;
        if (com.vehiclerental.config.Auth.isCustomer(session)) {
            tier = users.findById(com.vehiclerental.config.Auth.userId(session)).getTier();
        }
        return bookings.checkAvailability(id, pickup, returnDate, null, tier,
                Boolean.TRUE.equals(withDriver), pickupLocationId, dropLocationId, promo);
    }
    /** Used by the mini calendar (JSON): which days of the next few months are already taken. */
    @GetMapping("/api/vehicles/{id}/calendar")
    @ResponseBody
    public java.util.Map<String, Object> calendar(@PathVariable Long id,
                                                  @RequestParam(name = "from", required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam(name = "days", required = false) Integer days) {
        LocalDate start = (from == null || from.isBefore(LocalDate.now())) ? LocalDate.now() : from;
        int span = days == null ? 120 : Math.max(1, Math.min(days, 400));
        Vehicle vehicle = vehicles.findById(id);
        java.util.List<String> booked = new java.util.ArrayList<>();
        for (LocalDate day : bookings.bookedNights(id, start, start.plusDays(span))) {
            booked.add(day.toString());
        }
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("from", start.toString());
        out.put("days", span);
        out.put("maintenance", vehicle.isUnderMaintenance());
        out.put("booked", booked);
        return out;
    }
}
