package com.vehiclerental.admin.controller;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.user.dto.UserEditForm;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.UserStatus;
import com.vehiclerental.user.service.UserService;
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

/** Admin: manage registered customers (view, search, update, block, delete). */
@Controller
public class AdminUserController {

    private final UserService users;
    private final BookingService bookings;

    public AdminUserController(UserService users, BookingService bookings) {
        this.users = users;
        this.bookings = bookings;
    }

    @GetMapping("/admin/users")
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) UserStatus status, Model model) {
        model.addAttribute("users", users.search(q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("customer", users.findById(id));
        model.addAttribute("orders", bookings.forCustomer(id));
        return "admin/user-details";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Customer c = users.findById(id);
        UserEditForm form = new UserEditForm();
        form.setFullName(c.getFullName());
        form.setEmail(c.getEmail());
        form.setPhone(c.getPhone());
        form.setAddress(c.getAddress());
        form.setStatus(c.getStatus());
        form.setTier(c.getTier());
        form.setEmailVerified(c.isEmailVerified());
        model.addAttribute("userForm", form);
        model.addAttribute("tiers", com.vehiclerental.user.entity.CustomerTier.values());
        model.addAttribute("customer", c);
        model.addAttribute("statuses", UserStatus.values());
        return "admin/user-form";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("userForm") UserEditForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                users.updateByAdmin(id, form);
                Web.success(redirect, "Customer updated.");
                return "redirect:/admin/users";
            } catch (BusinessRuleException ex) {
                result.rejectValue("email", "rule", ex.getMessage());
            }
        }
        model.addAttribute("customer", users.findById(id));
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("tiers", com.vehiclerental.user.entity.CustomerTier.values());
        return "admin/user-form";
    }

    @PostMapping("/admin/users/{id}/block")
    public String block(@PathVariable Long id, @RequestParam boolean blocked, RedirectAttributes redirect) {
        users.setBlocked(id, blocked);
        Web.success(redirect, blocked ? "Customer blocked." : "Customer unblocked.");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        boolean hasOpen = bookings.forCustomer(id).stream().anyMatch(b -> b.isOpen());
        if (hasOpen) {
            Web.error(redirect, "This customer still has open bookings. Cancel or complete them first, or block the account instead.");
        } else {
            users.delete(id);
            Web.success(redirect, "Customer deleted.");
        }
        return "redirect:/admin/users";
    }
}
