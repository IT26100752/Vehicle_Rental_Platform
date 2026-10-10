package com.vehiclerental.config;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.common.util.SessionUtil;
import com.vehiclerental.user.entity.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes ${currentUser} and ${isAdmin} available in EVERY Thymeleaf template,
 * so the navbar can show the right links without repeating code.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        return SessionUtil.currentUser(session);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        return SessionUtil.currentUser(session) instanceof AdminUser;
    }
}
