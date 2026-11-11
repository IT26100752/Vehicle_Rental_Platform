package com.vehiclerental.user.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.user.service.VerificationService;
import com.vehiclerental.user.service.VerificationService.AccountState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Public pages for the email verification step: enter the 6 digit code, resend it. */
@Controller
public class VerificationController {

    private final VerificationService verification;

    public VerificationController(VerificationService verification) {
        this.verification = verification;
    }

    @GetMapping("/verify-email")
    public String page(@RequestParam(name = "email", required = false) String email, Model model) {
        AccountState state = verification.stateOf(email);
        model.addAttribute("unknownAccount", state == AccountState.UNKNOWN);
        model.addAttribute("alreadyVerified", state == AccountState.VERIFIED);
        if (state == AccountState.PENDING) {
            String address = email.trim().toLowerCase();
            model.addAttribute("verifyEmailAddress", address);
            model.addAttribute("resendWait", verification.secondsUntilResend(address));
            model.addAttribute("codeMinutes", verification.getCodeMinutes());
        }
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verify(@RequestParam("email") String email, @RequestParam("code") String code,
                         Model model, RedirectAttributes redirect) {
        try {
            verification.verify(email, code);
            model.addAttribute("verifiedEmail", email.trim().toLowerCase());     // the page plays the tick animation
            model.addAttribute("unknownAccount", false);
            model.addAttribute("alreadyVerified", false);
            return "verify-email";
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("verifyError", ex.getMessage());
            return back(email);
        }
    }

    @PostMapping("/verify-email/resend")
    public String resend(@RequestParam("email") String email, RedirectAttributes redirect) {
        try {
            verification.resend(email);
            Web.success(redirect, "A new code is on its way.");
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("verifyError", ex.getMessage());
        }
        return back(email);
    }

    private static String back(String email) {
        return "redirect:/verify-email?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
    }
}
