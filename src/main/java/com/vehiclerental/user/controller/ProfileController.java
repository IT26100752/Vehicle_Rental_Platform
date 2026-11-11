package com.vehiclerental.user.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.user.dto.PasswordChangeForm;
import com.vehiclerental.user.dto.ProfileForm;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Customer profile: view, update, change password, delete account. */
@Controller
public class ProfileController {

    private final UserService users;

    public ProfileController(UserService users) {
        this.users = users;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Customer customer = users.findById(Auth.userId(session));
        model.addAttribute("customer", customer);
        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setFullName(customer.getFullName());
            form.setPhone(customer.getPhone());
            form.setAddress(customer.getAddress());
            model.addAttribute("profileForm", form);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordChangeForm());
        }
        return "user/profile";
    }

    @PostMapping("/profile")
    public String update(@Valid @ModelAttribute("profileForm") ProfileForm form, BindingResult result,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        Long id = Auth.userId(session);
        if (result.hasErrors()) {
            model.addAttribute("customer", users.findById(id));
            model.addAttribute("passwordForm", new PasswordChangeForm());
            return "user/profile";
        }
        Customer updated = users.updateProfile(id, form);
        session.setAttribute(Auth.NAME, updated.getFullName());
        Web.success(redirect, "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordChangeForm form, BindingResult result,
                                 HttpSession session, Model model, RedirectAttributes redirect) {
        Long id = Auth.userId(session);
        if (!result.hasErrors()) {
            try {
                users.changePassword(id, form);
                Web.success(redirect, "Password changed.");
                return "redirect:/profile";
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        Customer customer = users.findById(id);
        ProfileForm profile = new ProfileForm();
        profile.setFullName(customer.getFullName());
        profile.setPhone(customer.getPhone());
        profile.setAddress(customer.getAddress());
        model.addAttribute("customer", customer);
        model.addAttribute("profileForm", profile);
        model.addAttribute("passwordTab", true);
        return "user/profile";
    }

    @PostMapping("/profile/delete")
    public String delete(HttpServletRequest request, HttpSession session, RedirectAttributes redirect) {
        users.delete(Auth.userId(session));
        Auth.signOut(request);
        Web.success(redirect, "Your account has been deleted.");
        return "redirect:/";
    }
}
