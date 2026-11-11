package com.vehiclerental.location.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.location.dto.LocationForm;
import com.vehiclerental.location.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin: manage the pickup / drop-off locations. */
@Controller
public class AdminLocationController {

    private final LocationService locations;

    public AdminLocationController(LocationService locations) {
        this.locations = locations;
    }

    @GetMapping("/admin/locations")
    public String list(Model model) {
        model.addAttribute("locations", locations.findAll());
        model.addAttribute("locationForm", new LocationForm());
        model.addAttribute("editing", false);
        return "admin/locations";
    }

    @GetMapping("/admin/locations/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("locations", locations.findAll());
        model.addAttribute("locationForm", LocationForm.from(locations.findById(id)));
        model.addAttribute("editing", true);
        return "admin/locations";
    }

    @PostMapping("/admin/locations")
    public String create(@Valid @ModelAttribute("locationForm") LocationForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("locations", locations.findAll());
            model.addAttribute("editing", false);
            return "admin/locations";
        }
        locations.create(form);
        Web.success(redirect, "Location added.");
        return "redirect:/admin/locations";
    }

    @PostMapping("/admin/locations/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("locationForm") LocationForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("locations", locations.findAll());
            model.addAttribute("editing", true);
            return "admin/locations";
        }
        locations.update(id, form);
        Web.success(redirect, "Location updated.");
        return "redirect:/admin/locations";
    }

    @PostMapping("/admin/locations/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        locations.delete(id);
        Web.success(redirect, "Location removed. Past bookings keep the name they were made with.");
        return "redirect:/admin/locations";
    }
}
