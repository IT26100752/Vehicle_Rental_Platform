package com.vehiclerental.common;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Helpers shared by the controllers. */
public final class Web {

    private Web() {
    }

    public static void success(RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute("success", message);
    }

    public static void error(RedirectAttributes redirect, String message) {
        redirect.addFlashAttribute("error", message);
    }

    /** First validation message, for pages that cannot show field errors inline. */
    public static String firstError(BindingResult result) {
        ObjectError first = result.getAllErrors().isEmpty() ? null : result.getAllErrors().get(0);
        return first == null ? "Please check the form" : first.getDefaultMessage();
    }
}
