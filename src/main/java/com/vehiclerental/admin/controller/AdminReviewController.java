package com.vehiclerental.admin.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.review.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin: moderate reviews (hide / show / delete). */
@Controller
public class AdminReviewController {

    private final ReviewService reviews;

    public AdminReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping("/admin/reviews")
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "rating", required = false) Integer rating, Model model) {
        model.addAttribute("reviewList", reviews.search(q, rating));
        model.addAttribute("q", q);
        model.addAttribute("rating", rating);
        return "admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/hide")
    public String hide(@PathVariable Long id, @RequestParam boolean hidden, RedirectAttributes redirect) {
        reviews.setHidden(id, hidden);
        Web.success(redirect, hidden ? "Review hidden from customers." : "Review is visible again.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        reviews.deleteByAdmin(id);
        Web.success(redirect, "Review deleted.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/reply")
    public String reply(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam("reply") String reply,
                        RedirectAttributes redirect) {
        try {
            reviews.reply(id, reply);
            Web.success(redirect, "Reply saved.");
        } catch (RuntimeException e) {
            Web.error(redirect, e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}
