package com.vehiclerental.complaint.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.complaint.dto.AdminUpdateForm;
import com.vehiclerental.complaint.dto.ReplyForm;
import com.vehiclerental.complaint.entity.Complaint;
import com.vehiclerental.complaint.entity.ComplaintPriority;
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

/** Admin side of complaints: tabs + search, thread, reply, status / priority, delete. */
@Controller
public class AdminComplaintController {

    private final ComplaintService complaints;

    public AdminComplaintController(ComplaintService complaints) {
        this.complaints = complaints;
    }

    @GetMapping("/admin/complaints")
    public String list(@RequestParam(name = "status", required = false) ComplaintStatus status,
                       @RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("complaintList", complaints.search(q, status));
        model.addAttribute("counts", complaints.countByStatus());
        model.addAttribute("total", complaints.search(null, null).size());
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("q", q);
        return "admin/complaints";
    }

    @GetMapping("/admin/complaints/{id}")
    public String details(@PathVariable Long id, Model model) {
        Complaint c = complaints.findById(id);
        model.addAttribute("complaint", c);
        model.addAttribute("thread", complaints.thread(id));
        model.addAttribute("booking", complaints.bookingOf(c));
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("priorities", ComplaintPriority.values());
        if (!model.containsAttribute("replyForm")) {
            model.addAttribute("replyForm", new ReplyForm());
        }
        return "admin/complaint-details";
    }

    @PostMapping("/admin/complaints/{id}/reply")
    public String reply(@PathVariable Long id, @Valid @ModelAttribute("replyForm") ReplyForm form,
                        BindingResult result, HttpSession session, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Web.error(redirect, Web.firstError(result));
        } else {
            complaints.replyAsAdmin(id, Auth.name(session), form.getMessage());
            Web.success(redirect, "Reply sent to the customer.");
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/admin/complaints/{id}/status")
    public String status(@PathVariable Long id, @ModelAttribute AdminUpdateForm form, RedirectAttributes redirect) {
        try {
            complaints.adminUpdate(id, form);
            Web.success(redirect, "Complaint updated.");
        } catch (BusinessRuleException ex) {
            Web.error(redirect, ex.getMessage());
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/admin/complaints/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        complaints.delete(id);
        Web.success(redirect, "Complaint deleted.");
        return "redirect:/admin/complaints";
    }
}
