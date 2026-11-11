package com.vehiclerental.complaint.controller;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.complaint.dto.ComplaintForm;
import com.vehiclerental.complaint.dto.ReplyForm;
import com.vehiclerental.complaint.entity.Complaint;
import com.vehiclerental.complaint.entity.ComplaintCategory;
import com.vehiclerental.complaint.entity.ComplaintStatus;
import com.vehiclerental.complaint.service.ComplaintService;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Customer side of complaints: list with status tabs, file, follow the thread, reply, reopen. */
@Controller
public class ComplaintController {

    private final ComplaintService complaints;
    private final BookingService bookings;

    public ComplaintController(ComplaintService complaints, BookingService bookings) {
        this.complaints = complaints;
        this.bookings = bookings;
    }

    @GetMapping("/complaints")
    public String list(@RequestParam(name = "status", required = false) ComplaintStatus status,
                       HttpSession session, Model model) {
        Long customerId = Auth.userId(session);
        model.addAttribute("complaintList", complaints.forCustomer(customerId, status));
        model.addAttribute("counts", complaints.countByStatus(customerId));
        model.addAttribute("total", complaints.forCustomer(customerId, null).size());
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("status", status);
        return "complaint/complaints";
    }

    @GetMapping("/complaints/new")
    public String form(@RequestParam(name = "bookingId", required = false) Long bookingId,
                       HttpSession session, Model model) {
        if (!model.containsAttribute("complaintForm")) {
            ComplaintForm form = new ComplaintForm();
            form.setBookingId(bookingId);
            model.addAttribute("complaintForm", form);
        }
        fillForm(model, session);
        return "complaint/complaint-form";
    }

    @PostMapping("/complaints")
    public String create(@Valid @ModelAttribute("complaintForm") ComplaintForm form, BindingResult result,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                Complaint saved = complaints.file(Auth.userId(session), form);
                Web.success(redirect, "Complaint " + saved.getReference() + " received. We will get back to you soon.");
                return "redirect:/complaints/" + saved.getId();
            } catch (BusinessRuleException ex) {
                result.reject("rule", ex.getMessage());
            }
        }
        fillForm(model, session);
        return "complaint/complaint-form";
    }

    @GetMapping("/complaints/{id}")
    public String details(@PathVariable Long id, HttpSession session, Model model) {
        Complaint c = complaints.findForCustomer(id, Auth.userId(session));
        model.addAttribute("complaint", c);
        model.addAttribute("thread", complaints.thread(id));
        model.addAttribute("booking", complaints.bookingOf(c));
        if (!model.containsAttribute("replyForm")) {
            model.addAttribute("replyForm", new ReplyForm());
        }
        return "complaint/complaint-details";
    }

    @PostMapping("/complaints/{id}/reply")
    public String reply(@PathVariable Long id, @Valid @ModelAttribute("replyForm") ReplyForm form,
                        BindingResult result, HttpSession session, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Web.error(redirect, Web.firstError(result));
            return "redirect:/complaints/" + id;
        }
        try {
            complaints.replyAsCustomer(Auth.userId(session), id, form.getMessage());
            Web.success(redirect, "Message sent.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/reopen")
    public String reopen(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            complaints.reopen(Auth.userId(session), id);
            Web.success(redirect, "Complaint reopened.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/complaints/" + id;
    }

    private void fillForm(Model model, HttpSession session) {
        model.addAttribute("categories", ComplaintCategory.values());
        model.addAttribute("myBookings", bookings.forCustomer(Auth.userId(session)));
    }
}
