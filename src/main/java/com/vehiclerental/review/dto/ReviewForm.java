package com.vehiclerental.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewForm {

    private Long bookingId;

    @NotNull(message = "Please choose a star rating")
    @Min(value = 1, message = "Rating must be 1 - 5 stars")
    @Max(value = 5, message = "Rating must be 1 - 5 stars")
    private Integer rating;

    @Size(max = 500, message = "Comment must be under 500 characters")
    private String comment;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
