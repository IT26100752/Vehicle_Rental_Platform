package com.vehiclerental.notification;

import com.vehiclerental.config.Auth;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * The bell: a customer's notification list (/notifications) and the administrators' list (/admin/notifications).
 * Opening an item marks it as read and forwards to the page it points to.
 */
@Controller
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    // ----------------------------------------------------------- customer

    @GetMapping("/notifications")
    public String customerList(HttpSession session, Model model) {
        model.addAttribute("notifications", notifications.forCustomer(Auth.userId(session)));
        model.addAttribute("adminView", false);
        return "notification/notifications";
    }

    @GetMapping("/notifications/{id}/open")
    public String customerOpen(@PathVariable Long id, HttpSession session) {
        Long customerId = Auth.userId(session);
        notifications.markRead(id, false, customerId);
        return "redirect:" + target(notifications.forCustomer(customerId), id, "/notifications");
    }

    @PostMapping("/notifications/read-all")
    public String customerReadAll(HttpSession session) {
        notifications.markAllRead(false, Auth.userId(session));
        return "redirect:/notifications";
    }

    // -------------------------------------------------------------- admin

    @GetMapping("/admin/notifications")
    public String adminList(Model model) {
        model.addAttribute("notifications", notifications.forAdmins());
        model.addAttribute("adminView", true);
        return "admin/notifications";
    }

    @GetMapping("/admin/notifications/{id}/open")
    public String adminOpen(@PathVariable Long id) {
        notifications.markRead(id, true, null);
        return "redirect:" + target(notifications.forAdmins(), id, "/admin/notifications");
    }

    @PostMapping("/admin/notifications/read-all")
    public String adminReadAll() {
        notifications.markAllRead(true, null);
        return "redirect:/admin/notifications";
    }

    // ---------------------------------------------------------- JSON badge

    /** Polled by the browser every 30 seconds to update the red number on the bell. */
    @GetMapping("/api/notifications/summary")
    @ResponseBody
    public Map<String, Object> summary(HttpSession session) {
        long unread = Auth.isAdmin(session) ? notifications.unreadForAdmins()
                : notifications.unreadForCustomer(Auth.userId(session));
        return Map.of("unread", unread);
    }

    /** The newest few notifications for the bell dropdown (customer or administrator, whoever is signed in). */
    @GetMapping("/api/notifications/recent")
    @ResponseBody
    public Map<String, Object> recent(HttpSession session) {
        boolean admin = Auth.isAdmin(session);
        java.util.List<Notification> all = admin ? notifications.forAdmins() : notifications.forCustomer(Auth.userId(session));
        java.time.format.DateTimeFormatter format = java.time.format.DateTimeFormatter.ofPattern("dd MMM, HH:mm");
        java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (Notification n : all) {
            if (items.size() >= 6) {
                break;
            }
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("title", n.getTitle());
            item.put("message", n.getMessage());
            item.put("type", n.getType().getLabel());
            item.put("css", n.getType().getCss());
            item.put("read", n.isRead());
            item.put("when", n.getCreatedAt() == null ? "" : n.getCreatedAt().format(format));
            item.put("url", (admin ? "/admin/notifications/" : "/notifications/") + n.getId() + "/open");
            items.add(item);
        }
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("admin", admin);
        out.put("items", items);
        return out;
    }

    private static String target(java.util.List<Notification> list, Long id, String fallback) {
        for (Notification n : list) {
            if (id.equals(n.getId()) && n.getLink() != null && n.getLink().startsWith("/") && !n.getLink().startsWith("//")) {
                return n.getLink();
            }
        }
        return fallback;
    }
}
