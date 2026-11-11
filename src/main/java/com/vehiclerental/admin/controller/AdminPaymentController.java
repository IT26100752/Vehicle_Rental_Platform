package com.vehiclerental.admin.controller;

import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.payment.controller.PaymentController;
import com.vehiclerental.payment.entity.Payment;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin: payment records and revenue. */
@Controller
public class AdminPaymentController {

    private final PaymentService payments;
    private final com.vehiclerental.admin.service.ActivityService activity;

    public AdminPaymentController(PaymentService payments, com.vehiclerental.admin.service.ActivityService activity) {
        this.payments = payments;
        this.activity = activity;
    }

    @GetMapping("/admin/payments")
    public String list(@RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "status", required = false) PaymentStatus status, Model model) {
        model.addAttribute("payments", payments.search(q, status));
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("revenueTotal", payments.totalRevenue());
        model.addAttribute("revenueCompleted", payments.revenueFromCompletedRentals());
        model.addAttribute("counts", payments.countByStatus());
        model.addAttribute("totalCount", payments.countByStatus().values().stream().mapToLong(Long::longValue).sum());
        return "admin/payments";
    }

    @PostMapping("/admin/payments/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        payments.delete(id);
        Web.success(redirect, "Payment record deleted.");
        return "redirect:/admin/payments";
    }

    @PostMapping("/admin/payments/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        try {
            payments.approve(id, Auth.name(session));
            activity.log(Auth.name(session), "Confirmed payment", "Payment #" + id);
            Web.success(redirect, "Payment confirmed. The booking is confirmed and the receipt was emailed.");
        } catch (RuntimeException e) {
            Web.error(redirect, e.getMessage());
        }
        return "redirect:/admin/payments?status=PENDING_VERIFICATION";
    }

    @PostMapping("/admin/payments/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(name = "reason", required = false) String reason,
                         HttpSession session, RedirectAttributes redirect) {
        try {
            payments.reject(id, Auth.name(session), reason);
            activity.log(Auth.name(session), "Rejected payment", "Payment #" + id);
            Web.success(redirect, "Payment rejected. The customer was told and can pay again.");
        } catch (RuntimeException e) {
            Web.error(redirect, e.getMessage());
        }
        return "redirect:/admin/payments?status=PENDING_VERIFICATION";
    }

    @GetMapping("/admin/payments/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        Payment payment = payments.findById(id);
        model.addAttribute("receipt", payments.receiptOf(payment));
        model.addAttribute("paymentId", id);
        model.addAttribute("adminView", true);
        model.addAttribute("justPaid", false);
        return "payment/receipt";
    }

    @GetMapping("/admin/payments/{id}/receipt.pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable Long id) {
        Payment payment = payments.findById(id);
        return PaymentController.pdfResponse(payments.receiptPdf(payment), payments.receiptFileName(payment));
    }
}
