package com.vehiclerental.notification;

import com.vehiclerental.common.BaseEntity;

/**
 * An in-app message shown under the bell icon.
 * recipientRole is "ADMIN" (recipientId null = every administrator) or "CUSTOMER" (recipientId = the customer).
 */
public class Notification extends BaseEntity {

    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER = "CUSTOMER";

    private String recipientRole = CUSTOMER;
    private Long recipientId;
    private NotificationType type = NotificationType.SYSTEM;
    private String title;
    private String message;
    /** Page to open when the notification is clicked, e.g. "/admin/complaints/4". */
    private String link;
    private boolean read;

    public boolean isForAdmins() {
        return ADMIN.equals(recipientRole);
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole == null ? CUSTOMER : recipientRole;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type == null ? NotificationType.SYSTEM : type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
