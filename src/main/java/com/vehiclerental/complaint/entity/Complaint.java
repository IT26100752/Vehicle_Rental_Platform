package com.vehiclerental.complaint.entity;

import com.vehiclerental.common.BaseEntity;

import java.time.LocalDateTime;

/** A customer complaint, optionally linked to one booking. Conversation lives in {@link ComplaintReply}. */
public class Complaint extends BaseEntity {

    private Long customerId;
    private String customerName;
    private Long bookingId;
    private ComplaintCategory category = ComplaintCategory.OTHER;
    private String subject;
    private String description;
    private ComplaintStatus status = ComplaintStatus.OPEN;
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;
    private String resolutionNote;
    private LocalDateTime resolvedAt;

    public String getReference() {
        if (getId() == null) {
            return "CMP-NEW";
        }
        int year = getCreatedAt() == null ? LocalDateTime.now().getYear() : getCreatedAt().getYear();
        return String.format("CMP-%d-%05d", year, getId());
    }

    /** A customer may reopen a resolved complaint for 7 days. */
    public boolean isReopenable() {
        return status == ComplaintStatus.RESOLVED && resolvedAt != null
                && resolvedAt.plusDays(7).isAfter(LocalDateTime.now());
    }

    public boolean isActive() { return status.isActive(); }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category == null ? ComplaintCategory.OTHER : category; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject == null ? null : subject.trim(); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? null : description.trim(); }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status == null ? ComplaintStatus.OPEN : status; }
    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority == null ? ComplaintPriority.MEDIUM : priority; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
