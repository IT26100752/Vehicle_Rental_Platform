package com.vehiclerental.user.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.user.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Public pages: ask for a reset link, then choose a new password. */
@Controller
public class PasswordResetController {

    private final PasswordResetService resets;

    public PasswordResetController(PasswordResetService resets) {
        this.resets = resets;
    }

    @GetMapping("/forgot-password")
    public String forgotPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@RequestParam String email, Model model) {
        resets.request(email == null ? "" : email.trim());
        model.addAttribute("sent", true);                       // same answer whether or not the account exists
        model.addAttribute("sentTo", email == null ? "" : email.trim());
        model.addAttribute("linkMinutes", resets.getLinkMinutes());
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam(name = "token", required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("validToken", resets.isValid(token));
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@RequestParam String token, @RequestParam String password, @RequestParam String confirm,
                        Model model, RedirectAttributes redirect) {
        try {
            resets.reset(token, password, confirm);
            Web.success(redirect, "Your password was changed. Sign in with the new password.");
            return "redirect:/login";
        } catch (BusinessRuleException ex) {
            model.addAttribute("token", token);
            model.addAttribute("validToken", resets.isValid(token));
            model.addAttribute("resetError", ex.getMessage());
            return "reset-password";
        }
    }
}
