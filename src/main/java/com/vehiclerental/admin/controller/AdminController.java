package com.vehiclerental.admin.controller;

import com.vehiclerental.admin.service.AdminService;
import com.vehiclerental.vehicle.service.VehicleService;
import com.vehiclerental.user.entity.CustomerUser;
import com.vehiclerental.user.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MEMBER 5 - Admin Management.
 * RE-USES the other services (as the intro session advises) and adds:
 *   dashboard statistics, user list, premium toggle, user delete.
 */
@Controller
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final VehicleService vehicleService;

    public AdminController(AdminService adminService, UserService userService,
                           VehicleService vehicleService) {
        this.adminService = adminService;
        this.userService = userService;
        this.vehicleService = vehicleService;
    }

    /* -------------------------- dashboard --------------------------- */
    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.dashboardStats());
        return "admin/dashboard";
    }

    /* -------------------------- fleet ------------------------------- */
    @GetMapping("/admin/vehicles")
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "admin/vehicles";
    }

    /* --------------------------- users ------------------------------ */
    @GetMapping("/users")
    public String users(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("users", userService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-premium")
    public String togglePremium(@PathVariable int id, RedirectAttributes redirect) {
        userService.findById(id).ifPresent(user -> {
            if (user instanceof CustomerUser c) {
                c.setPremium(!c.isPremium());
                userService.update(c);
                redirect.addFlashAttribute("success",
                        user.getFullName() + " is now " + (c.isPremium() ? "premium" : "regular"));
            }
        });
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable int id, RedirectAttributes redirect) {
        boolean ok = userService.delete(id);
        redirect.addFlashAttribute(ok ? "success" : "error",
                ok ? "User removed" : "User not found");
        return "redirect:/users";
    }
}
