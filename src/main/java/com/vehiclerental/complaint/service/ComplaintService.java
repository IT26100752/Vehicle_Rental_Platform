package com.vehiclerental.complaint.service;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.complaint.dto.AdminUpdateForm;
import com.vehiclerental.complaint.dto.ComplaintForm;
import com.vehiclerental.complaint.entity.Complaint;
import com.vehiclerental.complaint.entity.ComplaintCategory;
import com.vehiclerental.complaint.entity.ComplaintPriority;
import com.vehiclerental.complaint.entity.ComplaintReply;
import com.vehiclerental.complaint.entity.ComplaintStatus;
import com.vehiclerental.complaint.repository.ComplaintReplyRepository;
import com.vehiclerental.complaint.repository.ComplaintRepository;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.notification.EmailService;
import com.vehiclerental.notification.EmailTemplates;
import com.vehiclerental.notification.EmailType;
import com.vehiclerental.notification.NotificationService;
import com.vehiclerental.notification.NotificationType;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Complaints: customers file and follow them, admins answer, change status / priority and delete. */
@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final ComplaintReplyRepository replies;
    private final BookingService bookings;
    private final UserService users;
    private final NotificationService notifications;
    private final EmailService email;
    private final EmailTemplates templates;

    public ComplaintService(ComplaintRepository complaints, ComplaintReplyRepository replies, BookingService bookings,
                            UserService users, NotificationService notifications, EmailService email,
                            EmailTemplates templates) {
        this.complaints = complaints;
        this.replies = replies;
        this.bookings = bookings;
        this.users = users;
        this.notifications = notifications;
        this.email = email;
        this.templates = templates;
    }

    // ================================================================ Create

    public Complaint file(Long customerId, ComplaintForm form) {
        Customer customer = users.findById(customerId);
        if (form.getBookingId() != null) {
            bookings.findForCustomer(form.getBookingId(), customerId);   // must be the customer's own booking
        }
        Complaint c = new Complaint();
        c.setCustomerId(customerId);
        c.setCustomerName(customer.getFullName());
        c.setBookingId(form.getBookingId());
        c.setCategory(form.getCategory());
        c.setSubject(form.getSubject());
        c.setDescription(form.getDescription());
        c.setPriority(form.getCategory() == ComplaintCategory.DRIVER ? ComplaintPriority.HIGH : ComplaintPriority.MEDIUM);
        Complaint saved = complaints.create(c);

        notifications.notifyAdmins(NotificationType.COMPLAINT, "New complaint " + saved.getReference(),
                customer.getFullName() + " filed a " + saved.getCategory().getLabel().toLowerCase()
                        + " complaint: " + saved.getSubject(), "/admin/complaints/" + saved.getId());
        email.send(templates.builder("We received your complaint " + saved.getReference())
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("Thank you for telling us. Our team will review your complaint and reply as soon as possible.")
                .fact("Reference", saved.getReference())
                .fact("Category", saved.getCategory().getLabel())
                .fact("Subject", saved.getSubject())
                .button("Follow your complaint", "/complaints/" + saved.getId())
                .build(customer.getEmail(), EmailType.COMPLAINT));
        return saved;
    }

    public ComplaintReply replyAsCustomer(Long customerId, Long complaintId, String message) {
        Complaint c = findForCustomer(complaintId, customerId);
        if (c.getStatus() == ComplaintStatus.CLOSED) {
            throw new BusinessRuleException("This complaint is closed. Please file a new one.");
        }
        ComplaintReply reply = addReply(c, ComplaintReply.CUSTOMER, c.getCustomerName(), message);
        if (c.getStatus() == ComplaintStatus.RESOLVED) {      // customer answered a resolved complaint: back to work
            c.setStatus(ComplaintStatus.OPEN);
            c.setResolvedAt(null);
            complaints.update(c);
        }
        notifications.notifyAdmins(NotificationType.COMPLAINT, "Customer replied on " + c.getReference(),
                c.getCustomerName() + ": " + shorten(message), "/admin/complaints/" + c.getId());
        return reply;
    }

    public ComplaintReply replyAsAdmin(Long complaintId, String adminName, String message) {
        Complaint c = findById(complaintId);
        ComplaintReply reply = addReply(c, ComplaintReply.ADMIN, adminName, message);
        if (c.getStatus() == ComplaintStatus.OPEN) {
            c.setStatus(ComplaintStatus.IN_PROGRESS);
            complaints.update(c);
        }
        notifyCustomerOfUpdate(c, "New reply on " + c.getReference(), message);
        return reply;
    }

    // ================================================================== Read

    public Complaint findById(Long id) {
        return complaints.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Complaint", id));
    }

    public Complaint findForCustomer(Long id, Long customerId) {
        Complaint c = findById(id);
        if (!customerId.equals(c.getCustomerId())) {
            throw ResourceNotFoundException.of("Complaint", id);
        }
        return c;
    }

    public List<ComplaintReply> thread(Long complaintId) {
        return replies.findByComplaintId(complaintId).stream()
                .sorted(Comparator.comparing(ComplaintReply::getCreatedAt)).toList();
    }

    /** Customer's complaints, newest first, optionally limited to one status. */
    public List<Complaint> forCustomer(Long customerId, ComplaintStatus status) {
        return complaints.findByCustomerId(customerId).stream()
                .filter(c -> status == null || c.getStatus() == status)
                .sorted(newest()).toList();
    }

    public Map<ComplaintStatus, Long> countByStatus(Long customerId) {
        return count(customerId == null ? complaints.findAll() : complaints.findByCustomerId(customerId));
    }

    /** All complaints (admin) filtered by status and keyword (reference, subject, customer). */
    public List<Complaint> search(String keyword, ComplaintStatus status) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return complaints.findAll().stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> k.isEmpty()
                        || c.getReference().toLowerCase().contains(k)
                        || (c.getSubject() != null && c.getSubject().toLowerCase().contains(k))
                        || (c.getCustomerName() != null && c.getCustomerName().toLowerCase().contains(k)))
                .sorted(newest()).toList();
    }

    /** All-customer counts per status, for the admin tabs and dashboard. */
    public Map<ComplaintStatus, Long> countByStatus() {
        return countByStatus(null);
    }

    /** Complaints that still need staff attention (open + in progress). */
    public long countOpen() {
        return complaints.findAll().stream().filter(Complaint::isActive).count();
    }

    /** The booking a complaint refers to, or null. */
    public Booking bookingOf(Complaint c) {
        if (c.getBookingId() == null) {
            return null;
        }
        try {
            return bookings.findById(c.getBookingId());
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    // ================================================================ Update

    public Complaint reopen(Long customerId, Long complaintId) {
        Complaint c = findForCustomer(complaintId, customerId);
        if (!c.isReopenable()) {
            throw new BusinessRuleException("This complaint can no longer be reopened. Please file a new one.");
        }
        c.setStatus(ComplaintStatus.OPEN);
        c.setResolvedAt(null);
        complaints.update(c);
        notifications.notifyAdmins(NotificationType.COMPLAINT, "Complaint reopened " + c.getReference(),
                c.getCustomerName() + " reopened: " + c.getSubject(), "/admin/complaints/" + c.getId());
        return c;
    }

    public Complaint adminUpdate(Long complaintId, AdminUpdateForm form) {
        Complaint c = findById(complaintId);
        ComplaintStatus before = c.getStatus();
        if (form.getPriority() != null) {
            c.setPriority(form.getPriority());
        }
        if (form.getResolutionNote() != null && !form.getResolutionNote().isBlank()) {
            c.setResolutionNote(form.getResolutionNote().trim());
        }
        if (form.getStatus() != null) {
            c.setStatus(form.getStatus());
            if (form.getStatus() == ComplaintStatus.RESOLVED && before != ComplaintStatus.RESOLVED) {
                c.setResolvedAt(LocalDateTime.now());
            } else if (form.getStatus().isActive()) {
                c.setResolvedAt(null);
            }
        }
        complaints.update(c);
        if (c.getStatus() != before) {
            String text = "Your complaint is now " + c.getStatus().getLabel().toLowerCase() + "."
                    + (c.getResolutionNote() != null && c.getStatus() == ComplaintStatus.RESOLVED
                    ? " Resolution: " + c.getResolutionNote() : "");
            notifyCustomerOfUpdate(c, "Complaint " + c.getReference() + " is " + c.getStatus().getLabel().toLowerCase(), text);
        }
        return c;
    }

    // ================================================================ Delete

    public void delete(Long complaintId) {
        Complaint c = findById(complaintId);
        for (ComplaintReply r : replies.findByComplaintId(complaintId)) {
            replies.deleteById(r.getId());
        }
        complaints.deleteById(c.getId());
    }

    // ============================================================== internals

    private ComplaintReply addReply(Complaint c, String role, String author, String message) {
        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(c.getId());
        reply.setAuthorRole(role);
        reply.setAuthorName(author);
        reply.setMessage(message == null ? "" : message.trim());
        return replies.create(reply);
    }

    private void notifyCustomerOfUpdate(Complaint c, String title, String message) {
        notifications.notifyCustomer(c.getCustomerId(), NotificationType.COMPLAINT, title, shorten(message),
                "/complaints/" + c.getId());
        Customer customer = users.findById(c.getCustomerId());
        email.send(templates.builder(title)
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph(message)
                .fact("Reference", c.getReference())
                .fact("Status", c.getStatus().getLabel())
                .button("Open your complaint", "/complaints/" + c.getId())
                .build(customer.getEmail(), EmailType.COMPLAINT));
    }

    private static Map<ComplaintStatus, Long> count(List<Complaint> list) {
        Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
        for (ComplaintStatus s : ComplaintStatus.values()) {
            counts.put(s, 0L);
        }
        for (Complaint c : list) {
            counts.merge(c.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    private static String shorten(String s) {
        if (s == null) {
            return "";
        }
        return s.length() <= 140 ? s : s.substring(0, 137) + "...";
    }

    private static Comparator<Complaint> newest() {
        return Comparator.comparing(Complaint::getCreatedAt).reversed();
    }
}
