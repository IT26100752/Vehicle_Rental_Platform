package com.vehiclerental.user.service;

import com.vehiclerental.booking.service.PremiumUnlockedEvent;
import com.vehiclerental.notification.EmailService;
import com.vehiclerental.notification.EmailTemplates;
import com.vehiclerental.notification.EmailType;
import com.vehiclerental.notification.NotificationService;
import com.vehiclerental.notification.NotificationType;
import com.vehiclerental.user.entity.Customer;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Tells a customer (in the app and by email) that they just became a premium member. */
@Component
public class PremiumWelcomeListener {

    private final UserService users;
    private final NotificationService notifications;
    private final EmailService email;
    private final EmailTemplates templates;

    public PremiumWelcomeListener(UserService users, NotificationService notifications, EmailService email,
                                  EmailTemplates templates) {
        this.users = users;
        this.notifications = notifications;
        this.email = email;
        this.templates = templates;
    }

    @EventListener
    public void onPremiumUnlocked(PremiumUnlockedEvent event) {
        Customer customer = users.findById(event.getCustomerId());
        notifications.notifyCustomer(customer.getId(), NotificationType.ACCOUNT, "Welcome to Premium",
                "You passed the booking limit, so your account is now Premium: cheaper rates, premium-only promo codes and gentler late fees.",
                "/dashboard");
        email.send(templates.builder("You are now a Premium member")
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("Thank you for renting with us so often. Your account is now Premium.")
                .fact("Rental rate", "10% off every rental, 15% off for 7+ days")
                .fact("Late fees", "Gentler late-return fines")
                .fact("Promo codes", "Premium-only codes unlocked")
                .button("Open my dashboard", "/dashboard")
                .build(customer.getEmail(), EmailType.GENERAL));
    }
}
