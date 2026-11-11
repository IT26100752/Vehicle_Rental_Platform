package com.vehiclerental.review.entity;

import com.vehiclerental.common.BaseEntity;

/** A customer's rating (1-5 stars) and optional comment for a completed rental. */
public class Review extends BaseEntity {

    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicleModel;
    private int rating;
    private String comment;
    /** Set by an admin to hide an inappropriate review without deleting it. */
    private boolean hidden;
    private String adminReply;
    private java.time.LocalDateTime repliedAt;

    /** Low ratings are published like any other; this only drives the admin alert and the "raise a complaint" offer. */
    public boolean isLow() { return rating <= 2; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public java.time.LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(java.time.LocalDateTime repliedAt) { this.repliedAt = repliedAt; }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null || comment.isBlank() ? null : comment.trim();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
