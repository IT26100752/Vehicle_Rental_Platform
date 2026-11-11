package com.vehiclerental.admin.controller;

import com.vehiclerental.admin.entity.Admin;
import com.vehiclerental.admin.service.ActivityService;
import com.vehiclerental.admin.service.AdminService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin team (everyone can see it; only the manager admin can add, delete or reset),
 * the activity log, and "my account" (change own password).
 */
@Controller
public class AdminAccountController {

    private final AdminService admins;
    private final ActivityService activity;

    public AdminAccountController(AdminService admins, ActivityService activity) {
        this.admins = admins;
        this.activity = activity;
    }

    @GetMapping("/admin/admins")
    public String team(HttpSession session, Model model) {
        model.addAttribute("team", admins.findAll());
        model.addAttribute("meId", Auth.userId(session));
        return "admin/admins";
    }

    @PostMapping("/admin/admins")
    public String add(@RequestParam String username, @RequestParam String fullName,
                      @RequestParam(required = false) String email, @RequestParam String password,
                      HttpSession session, RedirectAttributes redirect) {
        try {
            Admin created = admins.addAdmin(me(session), username, fullName, email, password);
            Web.success(redirect, "Admin @" + created.getUsername() + " was added.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/admins";
    }

    @PostMapping("/admin/admins/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            admins.deleteAdmin(me(session), id);
            Web.success(redirect, "Admin deleted.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/admins";
    }

    @PostMapping("/admin/admins/{id}/reset-password")
    public String reset(@PathVariable Long id, @RequestParam String password,
                        HttpSession session, RedirectAttributes redirect) {
        try {
            admins.resetPassword(me(session), id, password);
            Web.success(redirect, "Password reset.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/admins";
    }

    @GetMapping("/admin/activity")
    public String log(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("entries", activity.search(q, 300));
        model.addAttribute("q", q);
        return "admin/activity";
    }

    @GetMapping("/admin/account")
    public String account(HttpSession session, Model model) {
        model.addAttribute("me", me(session));
        return "admin/account";
    }

    @PostMapping("/admin/account/password")
    public String changePassword(@RequestParam String current, @RequestParam String password,
                                 @RequestParam String confirm, HttpSession session, RedirectAttributes redirect) {
        try {
            if (!password.equals(confirm)) {
                throw new BusinessRuleException("The two new passwords do not match");
            }
            admins.changeOwnPassword(Auth.userId(session), current, password);
            Web.success(redirect, "Your password was changed.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/account";
    }

    private Admin me(HttpSession session) {
        return admins.findById(Auth.userId(session));
    }
}
