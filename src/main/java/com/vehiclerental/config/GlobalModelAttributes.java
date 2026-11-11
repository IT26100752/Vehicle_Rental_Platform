package com.vehiclerental.config;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** Values every template needs (navbar state, today's date, the late-fee rule ...). */
@ControllerAdvice
public class GlobalModelAttributes {

    private final BookingService bookings;
    private final NotificationService notifications;
    private final com.vehiclerental.user.service.UserService users;

    public GlobalModelAttributes(BookingService bookings, NotificationService notifications,
                                 com.vehiclerental.user.service.UserService users) {
        this.users = users;
        this.bookings = bookings;
        this.notifications = notifications;
    }

    @ModelAttribute
    public void addCommon(Model model, HttpServletRequest request) {
        model.addAllAttributes(values(request));
    }

    /** Also used by the error handler, because @ModelAttribute methods do not run for @ExceptionHandler. */
    public Map<String, Object> values(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> values = new HashMap<>();
        values.put("isAdmin", Auth.isAdmin(session));
        values.put("isManager", Auth.isManager(session));
        values.put("isCustomer", Auth.isCustomer(session));
        values.put("currentUserName", session == null ? "" : Auth.name(session));
        values.put("today", LocalDate.now());
        values.put("lateFeeRule", bookings.getLateFeePolicy().describe());
        values.put("holdMinutes", bookings.getHoldMinutes());
        long unread = 0;
        if (Auth.isAdmin(session)) {
            unread = notifications.unreadForAdmins();
        } else if (Auth.isCustomer(session)) {
            unread = notifications.unreadForCustomer(Auth.userId(session));
        }
        values.put("unreadNotifications", unread);
        boolean premium = false;
        if (Auth.isCustomer(session)) {
            try {
                premium = users.findById(Auth.userId(session)).getTier() == com.vehiclerental.user.entity.CustomerTier.PREMIUM;
            } catch (RuntimeException gone) {
                premium = false;
            }
        }
        values.put("isPremium", premium);
        return values;
    }
}
