package com.vehiclerental.notification;

import com.vehiclerental.admin.entity.Admin;
import com.vehiclerental.admin.repository.AdminRepository;
import com.vehiclerental.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * In-app notifications (the bell) plus the matching alert email for administrators.
 *
 *  - {@link #notifyAdmins}: something the staff must act on (new booking, payment to verify, complaint, bad review ...).
 *    One notification is shared by all admins, and a short email goes to the alert address.
 *  - {@link #notifyCustomer}: a message for one customer (in-app only - the caller sends the detailed email).
 */
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final AdminRepository admins;
    private final EmailService email;
    private final EmailTemplates templates;
    private final boolean emailAdmins;
    private final String alertAddress;

    public NotificationService(NotificationRepository repository, AdminRepository admins, EmailService email,
                               EmailTemplates templates,
                               @Value("${app.notify.admin-email:true}") boolean emailAdmins,
                               @Value("${app.notify.admin-address:}") String alertAddress) {
        this.repository = repository;
        this.admins = admins;
        this.email = email;
        this.templates = templates;
        this.emailAdmins = emailAdmins;
        this.alertAddress = alertAddress == null ? "" : alertAddress.trim();
    }

    // ================================================================ Create

    /** Tell every administrator (bell + alert email). */
    public Notification notifyAdmins(NotificationType type, String title, String message, String link) {
        Notification n = new Notification();
        n.setRecipientRole(Notification.ADMIN);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        Notification saved = repository.create(n);
        trim();
        if (emailAdmins) {
            for (String address : adminAddresses()) {
                email.send(templates.builder("Admin alert: " + title)
                        .greeting("Hello,")
                        .paragraph(message)
                        .button("Open in the admin panel", link == null ? "/admin/dashboard" : link)
                        .build(address, EmailType.ADMIN_ALERT));
            }
        }
        return saved;
    }

    /** Tell one customer (bell only). */
    public Notification notifyCustomer(Long customerId, NotificationType type, String title, String message, String link) {
        Notification n = new Notification();
        n.setRecipientRole(Notification.CUSTOMER);
        n.setRecipientId(customerId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        Notification saved = repository.create(n);
        trim();
        return saved;
    }

    // ================================================================== Read

    public List<Notification> forAdmins() {
        return repository.findForAdmins().stream().sorted(newest()).toList();
    }

    public List<Notification> forCustomer(Long customerId) {
        return repository.findForCustomer(customerId).stream().sorted(newest()).toList();
    }

    public long unreadForAdmins() {
        return repository.findForAdmins().stream().filter(n -> !n.isRead()).count();
    }

    public long unreadForCustomer(Long customerId) {
        return repository.findForCustomer(customerId).stream().filter(n -> !n.isRead()).count();
    }

    // ================================================================ Update

    public void markRead(Long id, boolean admin, Long customerId) {
        Notification n = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        if (admin ? !n.isForAdmins() : (n.isForAdmins() || !customerId.equals(n.getRecipientId()))) {
            throw ResourceNotFoundException.of("Notification", id);
        }
        if (!n.isRead()) {
            n.setRead(true);
            repository.update(n);
        }
    }

    public void markAllRead(boolean admin, Long customerId) {
        List<Notification> list = admin ? repository.findForAdmins() : repository.findForCustomer(customerId);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                repository.update(n);
            }
        }
    }

    // ================================================================ Delete

    /** Remove read notifications older than the newest 200 so the file does not grow forever. */
    public void trim() {
        List<Notification> all = repository.findAll().stream().sorted(newest()).toList();
        for (int i = 200; i < all.size(); i++) {
            if (all.get(i).isRead()) {
                repository.deleteById(all.get(i).getId());
            }
        }
    }

    // ============================================================== internals

    private Set<String> adminAddresses() {
        Set<String> result = new LinkedHashSet<>();
        if (!alertAddress.isEmpty()) {
            result.add(alertAddress);
            return result;
        }
        for (Admin a : admins.findAll()) {
            if (a.getEmail() != null && !a.getEmail().isBlank()) {
                result.add(a.getEmail());
            }
        }
        return result;
    }

    private static Comparator<Notification> newest() {
        return Comparator.comparing(Notification::getCreatedAt).reversed();
    }
}
