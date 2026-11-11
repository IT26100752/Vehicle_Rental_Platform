package com.vehiclerental.user.controller;

import com.vehiclerental.admin.entity.Admin;
import com.vehiclerental.admin.service.AdminService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.user.dto.LoginRequest;
import com.vehiclerental.user.dto.RegisterRequest;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.EmailNotVerifiedException;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.user.service.VerificationService;
import com.vehiclerental.user.service.VerificationService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Registration, customer + administrator sign-in, sign-out. */
@Controller
public class AuthController {

    private final UserService users;
    private final AdminService admins;
    private final VerificationService verification;

    public AuthController(UserService users, AdminService admins, VerificationService verification) {
        this.users = users;
        this.admins = admins;
        this.verification = verification;
    }

    // ------------------------------------------------------------- login

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "type", required = false) String type,
                            @RequestParam(name = "required", required = false) String required,
                            @RequestParam(name = "blocked", required = false) String blocked,
                            HttpSession session, Model model) {
        if (Auth.isCustomer(session)) {
            return "redirect:/dashboard";
        }
        if (Auth.isAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        model.addAttribute("adminTab", "admin".equals(type));
        if (blocked != null) {
            model.addAttribute("loginError", "Your account has been blocked. Please contact the rental company.");
        } else if (required != null) {
            model.addAttribute("loginNotice", "Please sign in to continue.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest request, BindingResult result,
                        HttpServletRequest http, Model model) {
        model.addAttribute("adminTab", false);
        if (result.hasErrors()) {
            return "login";
        }
        try {
            Customer customer = users.authenticate(request.getEmail(), request.getPassword());
            HttpSession old = http.getSession(false);
            String saved = old == null ? null : (String) old.getAttribute(Auth.SAVED_URL);
            Auth.signIn(http, customer);
            http.getSession().removeAttribute(Auth.SAVED_URL);
            return "redirect:" + (saved != null && saved.startsWith("/") && !saved.startsWith("//") ? saved : "/dashboard");
        } catch (EmailNotVerifiedException ex) {
            model.addAttribute("loginError", ex.getMessage());
            model.addAttribute("unverifiedEmail", ex.getEmail());
            return "login";
        } catch (BusinessRuleException ex) {
            model.addAttribute("loginError", ex.getMessage());
            return "login";
        }
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username, @RequestParam String password,
                             HttpServletRequest http, RedirectAttributes redirect) {
        try {
            Admin admin = admins.authenticate(username, password);
            Auth.signIn(http, admin);
            return "redirect:/admin/dashboard";
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
            return "redirect:/login?type=admin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirect) {
        Auth.signOut(request);
        Web.success(redirect, "You have been signed out.");
        return "redirect:/login";
    }

    // ---------------------------------------------------------- register

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (Auth.isCustomer(session)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request, BindingResult result,
                           RedirectAttributes redirect) {
        if (!result.hasFieldErrors("email") && users.emailTaken(request.getEmail())) {
            result.rejectValue("email", "taken", "This email is already registered");
        }
        if (!result.hasFieldErrors("confirmPassword") && !result.hasFieldErrors("password")
                && !request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "The two passwords do not match");
        }
        if (result.hasErrors()) {
            return "register";
        }
        Customer customer;
        try {
            customer = users.register(request);
        } catch (BusinessRuleException ex) {
            result.rejectValue("email", "rule", ex.getMessage());
            return "register";
        }
        try {
            verification.issue(customer);
            Web.success(redirect, "Account created! We sent a 6-digit code to " + customer.getEmail() + ".");
        } catch (BusinessRuleException ex) {
            // cool-down still running (re-registered a moment ago): the earlier code is still valid
            Web.success(redirect, "We already sent a code to " + customer.getEmail() + ". Please enter it below.");
        }
        return "redirect:/verify-email?email=" + URLEncoder.encode(customer.getEmail(), StandardCharsets.UTF_8);
    }
}
