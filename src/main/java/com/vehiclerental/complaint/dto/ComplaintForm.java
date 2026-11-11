package com.vehiclerental.complaint.dto;

import com.vehiclerental.complaint.entity.ComplaintCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Customer's new complaint. bookingId is optional. */
public class ComplaintForm {

    private Long bookingId;

    @NotNull(message = "Choose a category")
    private ComplaintCategory category = ComplaintCategory.OTHER;

    @NotBlank(message = "Enter a short subject")
    @Size(min = 4, max = 100, message = "Subject must be 4 - 100 characters")
    private String subject;

    @NotBlank(message = "Describe what went wrong")
    @Size(min = 10, max = 1500, message = "Description must be 10 - 1500 characters")
    private String description;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
