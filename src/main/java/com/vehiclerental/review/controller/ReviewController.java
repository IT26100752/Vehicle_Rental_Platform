package com.vehiclerental.review.controller;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.review.dto.ReviewForm;
import com.vehiclerental.review.entity.Review;
import com.vehiclerental.review.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Public review wall plus the signed-in customer's own reviews (write / edit / delete). */
@Controller
public class ReviewController {

    private final ReviewService reviews;
    private final BookingService bookings;

    public ReviewController(ReviewService reviews, BookingService bookings) {
        this.reviews = reviews;
        this.bookings = bookings;
    }

    @GetMapping("/reviews")
    public String all(Model model) {
        model.addAttribute("reviewList", reviews.visible());
        return "review/reviews";
    }

    @GetMapping("/reviews/my")
    public String mine(HttpSession session, Model model) {
        model.addAttribute("reviewList", reviews.forCustomer(Auth.userId(session)));
        return "review/my-reviews";
    }

    @GetMapping("/reviews/new/{bookingId}")
    public String newForm(@PathVariable Long bookingId, HttpSession session, Model model, RedirectAttributes redirect) {
        Booking booking = bookings.findForCustomer(bookingId, Auth.userId(session));
        if (reviews.existsForBooking(bookingId)) {
            Web.error(redirect, "You have already reviewed this rental.");
            return "redirect:/reviews/my";
        }
        ReviewForm form = new ReviewForm();
        form.setBookingId(bookingId);
        form.setRating(5);
        model.addAttribute("reviewForm", form);
        model.addAttribute("booking", booking);
        model.addAttribute("editing", false);
        return "review/review-form";
    }

    @PostMapping("/reviews")
    public String create(@Valid @ModelAttribute("reviewForm") ReviewForm form, BindingResult result,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        Long customerId = Auth.userId(session);
        if (!result.hasErrors()) {
            try {
                reviews.create(customerId, form);
                Web.success(redirect, "Thank you for your review!");
                return "redirect:/reviews/my";
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        model.addAttribute("booking", bookings.findForCustomer(form.getBookingId(), customerId));
        model.addAttribute("editing", false);
        return "review/review-form";
    }

    @GetMapping("/reviews/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Review review = reviews.findForCustomer(id, Auth.userId(session));
        ReviewForm form = new ReviewForm();
        form.setBookingId(review.getBookingId());
        form.setRating(review.getRating());
        form.setComment(review.getComment());
        model.addAttribute("reviewForm", form);
        model.addAttribute("review", review);
        model.addAttribute("booking", bookings.findForCustomer(review.getBookingId(), Auth.userId(session)));
        model.addAttribute("editing", true);
        return "review/review-form";
    }

    @PostMapping("/reviews/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("reviewForm") ReviewForm form,
                         BindingResult result, HttpSession session, Model model, RedirectAttributes redirect) {
        Long customerId = Auth.userId(session);
        if (!result.hasErrors()) {
            reviews.update(customerId, id, form);
            Web.success(redirect, "Review updated.");
            return "redirect:/reviews/my";
        }
        Review review = reviews.findForCustomer(id, customerId);
        model.addAttribute("review", review);
        model.addAttribute("booking", bookings.findForCustomer(review.getBookingId(), customerId));
        model.addAttribute("editing", true);
        return "review/review-form";
    }

    @PostMapping("/reviews/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        reviews.deleteOwn(Auth.userId(session), id);
        Web.success(redirect, "Review deleted.");
        return "redirect:/reviews/my";
    }
}
