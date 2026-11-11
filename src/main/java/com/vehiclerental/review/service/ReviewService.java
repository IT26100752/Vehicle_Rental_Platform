package com.vehiclerental.review.service;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.review.dto.ReviewForm;
import com.vehiclerental.review.entity.Review;
import com.vehiclerental.review.repository.ReviewRepository;
import com.vehiclerental.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

/** Reviews: customers write / edit / delete their own, admins can hide or delete any. */
@Service
public class ReviewService {

    private final ReviewRepository repository;
    private final BookingService bookings;
    private final UserService users;
    private final com.vehiclerental.notification.NotificationService notifications;
    private final com.vehiclerental.notification.EmailService email;
    private final com.vehiclerental.notification.EmailTemplates templates;

    public ReviewService(ReviewRepository repository, BookingService bookings, UserService users,
                         com.vehiclerental.notification.NotificationService notifications,
                         com.vehiclerental.notification.EmailService email,
                         com.vehiclerental.notification.EmailTemplates templates) {
        this.notifications = notifications;
        this.email = email;
        this.templates = templates;
        this.repository = repository;
        this.bookings = bookings;
        this.users = users;
    }

    // ================================================================ Create

    public Review create(Long customerId, ReviewForm form) {
        Booking booking = bookings.findForCustomer(form.getBookingId(), customerId);
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessRuleException("You can review a rental once it has been completed");
        }
        if (repository.findByBookingId(booking.getId()).isPresent()) {
            throw new BusinessRuleException("You have already reviewed this rental");
        }
        Review review = new Review();
        review.setBookingId(booking.getId());
        review.setCustomerId(customerId);
        review.setCustomerName(users.findById(customerId).getFullName());
        review.setVehicleId(booking.getVehicleId());
        review.setVehicleModel(booking.getVehicleModel());
        review.setRating(form.getRating());
        review.setComment(form.getComment());
        Review saved = repository.create(review);
        // Every honest review is published straight away, good or bad. Low ones also alert the staff.
        if (saved.isLow()) {
            notifications.notifyAdmins(com.vehiclerental.notification.NotificationType.REVIEW,
                    saved.getRating() + "-star review for " + saved.getVehicleModel(),
                    saved.getCustomerName() + " left " + saved.getRating() + " star(s)"
                            + (saved.getComment() == null ? "." : ": \"" + saved.getComment() + "\"")
                            + " You can reply publicly from the Reviews page.", "/admin/reviews");
        }
        return saved;
    }

    /** Admin: a public reply shown under the review. The customer is told. */
    public Review reply(Long reviewId, String text) {
        Review review = findById(reviewId);
        String reply = text == null ? "" : text.trim();
        if (reply.isEmpty()) {
            review.setAdminReply(null);
            review.setRepliedAt(null);
            return repository.update(review);
        }
        if (reply.length() > 400) {
            throw new BusinessRuleException("A reply must be under 400 characters");
        }
        review.setAdminReply(reply);
        review.setRepliedAt(java.time.LocalDateTime.now());
        Review saved = repository.update(review);
        notifications.notifyCustomer(saved.getCustomerId(), com.vehiclerental.notification.NotificationType.REVIEW,
                "We replied to your review", "Our team replied to your review of the " + saved.getVehicleModel() + ".", "/reviews/my");
        email.send(templates.builder("We replied to your review")
                .greeting("Hello " + saved.getCustomerName() + ",")
                .paragraph("Thank you for your feedback on the " + saved.getVehicleModel() + ". Our reply:")
                .highlight("Reply", reply)
                .button("See my reviews", "/reviews/my")
                .build(users.findById(saved.getCustomerId()).getEmail(), com.vehiclerental.notification.EmailType.REVIEW));
        return saved;
    }

    public long countLow() {
        return repository.findAll().stream().filter(Review::isLow).filter(r -> r.getAdminReply() == null).count();
    }

    // ================================================================== Read

    public Review findById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Review", id));
    }

    public Review findForCustomer(Long reviewId, Long customerId) {
        Review review = findById(reviewId);
        if (!customerId.equals(review.getCustomerId())) {
            throw ResourceNotFoundException.of("Review", reviewId);
        }
        return review;
    }

    public boolean existsForBooking(Long bookingId) {
        return repository.findByBookingId(bookingId).isPresent();
    }

    /** Reviews everyone can see (not hidden), newest first. */
    public List<Review> visible() {
        return repository.findAll().stream().filter(r -> !r.isHidden()).sorted(newest()).toList();
    }

    public List<Review> visibleForVehicle(Long vehicleId) {
        return repository.findByVehicleId(vehicleId).stream().filter(r -> !r.isHidden()).sorted(newest()).toList();
    }

    public List<Review> forCustomer(Long customerId) {
        return repository.findByCustomerId(customerId).stream().sorted(newest()).toList();
    }

    public List<Review> search(String keyword, Integer rating) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findAll().stream()
                .filter(r -> rating == null || r.getRating() == rating)
                .filter(r -> k.isEmpty()
                        || (r.getComment() != null && r.getComment().toLowerCase().contains(k))
                        || (r.getCustomerName() != null && r.getCustomerName().toLowerCase().contains(k))
                        || (r.getVehicleModel() != null && r.getVehicleModel().toLowerCase().contains(k)))
                .sorted(newest()).toList();
    }

    public double averageRating(Long vehicleId) {
        OptionalDouble avg = visibleForVehicle(vehicleId).stream().mapToInt(Review::getRating).average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    public int count(Long vehicleId) {
        return visibleForVehicle(vehicleId).size();
    }

    public long total() {
        return repository.count();
    }

    // ================================================================ Update

    public Review update(Long customerId, Long reviewId, ReviewForm form) {
        Review review = findForCustomer(reviewId, customerId);
        review.setRating(form.getRating());
        review.setComment(form.getComment());
        return repository.update(review);
    }

    /** Admin: hide an inappropriate review (or show it again) without deleting it. */
    public Review setHidden(Long reviewId, boolean hidden) {
        Review review = findById(reviewId);
        review.setHidden(hidden);
        return repository.update(review);
    }

    // ================================================================ Delete

    public void deleteOwn(Long customerId, Long reviewId) {
        findForCustomer(reviewId, customerId);
        repository.deleteById(reviewId);
    }

    public void deleteByAdmin(Long reviewId) {
        if (!repository.deleteById(reviewId)) {
            throw ResourceNotFoundException.of("Review", reviewId);
        }
    }

    private static Comparator<Review> newest() {
        return Comparator.comparing(Review::getCreatedAt).reversed();
    }
}
