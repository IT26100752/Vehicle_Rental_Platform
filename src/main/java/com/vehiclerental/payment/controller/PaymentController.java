package com.vehiclerental.payment.controller;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.Web;
import com.vehiclerental.config.Auth;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.processor.PaymentFieldException;
import com.vehiclerental.payment.processor.WalletPaymentProcessor;
import com.vehiclerental.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer payments: choose a method and pay (JSON endpoint for the verification animation, plus a plain form
 * fallback), payment history, on-screen receipt and PDF download.
 */
@Controller
public class PaymentController {

    private final PaymentService payments;
    private final BookingService bookings;
    private final String bankName;
    private final String bankAccountName;
    private final String bankAccountNumber;
    private final String bankBranch;

    public PaymentController(PaymentService payments, BookingService bookings,
                             @Value("${app.bank.name:}") String bankName,
                             @Value("${app.bank.account-name:}") String bankAccountName,
                             @Value("${app.bank.account-number:}") String bankAccountNumber,
                             @Value("${app.bank.branch:}") String bankBranch) {
        this.payments = payments;
        this.bookings = bookings;
        this.bankName = bankName;
        this.bankAccountName = bankAccountName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankBranch = bankBranch;
    }

    // ------------------------------------------------------------- pay page

    @GetMapping("/payments/booking/{bookingId}")
    public String paymentPage(@PathVariable Long bookingId, HttpSession session, Model model) {
        Booking booking = bookings.findForCustomer(bookingId, Auth.userId(session));
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT && booking.getStatus() != BookingStatus.FINE_DUE) {
            return "redirect:/bookings/" + bookingId;
        }
        if (!model.containsAttribute("paymentForm")) {
            model.addAttribute("paymentForm", new PaymentForm());
        }
        fill(model, booking);
        return "payment/payment";
    }

    /** Plain form post: used when JavaScript is off or the JSON call could not be made. */
    @PostMapping("/payments/booking/{bookingId}")
    public String pay(@PathVariable Long bookingId, @ModelAttribute("paymentForm") PaymentForm form,
                      HttpSession session, Model model, RedirectAttributes redirect) {
        Long customerId = Auth.userId(session);
        try {
            Payment saved = payments.pay(customerId, bookingId, form);
            if (saved.getStatus() == PaymentStatus.FAILED) {
                model.addAttribute("fieldErrors", Map.of());
                model.addAttribute("paymentError", saved.getFailureReason());
                fill(model, bookings.findForCustomer(bookingId, customerId));
                return "payment/payment";
            }
            return "redirect:/payments/" + saved.getId() + "/receipt?paid=1";
        } catch (PaymentFieldException ex) {
            model.addAttribute("fieldErrors", ex.getErrors());
            model.addAttribute("paymentError", ex.getMessage());
        } catch (BusinessRuleException ex) {
            model.addAttribute("fieldErrors", Map.of());
            model.addAttribute("paymentError", ex.getMessage());
        }
        fill(model, bookings.findForCustomer(bookingId, customerId));
        return "payment/payment";
    }

    /**
     * JSON endpoint behind the "Verifying your payment" overlay.
     * {ok, status, message, redirectUrl, errors}. ok=true for PAID and PENDING_VERIFICATION.
     */
    @PostMapping("/payments/booking/{bookingId}/process")
    @ResponseBody
    public Map<String, Object> process(@PathVariable Long bookingId, @ModelAttribute PaymentForm form, HttpSession session) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            Payment saved = payments.pay(Auth.userId(session), bookingId, form);
            out.put("status", saved.getStatus().name());
            switch (saved.getStatus()) {
                case PAID -> {
                    out.put("ok", true);
                    out.put("message", "Your booking is confirmed. Your receipt is ready.");
                    out.put("redirectUrl", "/payments/" + saved.getId() + "/receipt?paid=1");
                }
                case PENDING_VERIFICATION -> {
                    out.put("ok", true);
                    out.put("message", saved.getMethodType() == PaymentMethod.CASH
                            ? "Your vehicle is reserved. Please pay in cash at the counter and we will confirm your booking."
                            : "We will verify your bank transfer and confirm the booking. You will be notified.");
                    out.put("redirectUrl", "/payments/" + saved.getId() + "/receipt?paid=1");
                }
                default -> {
                    out.put("ok", false);
                    out.put("message", saved.getFailureReason() == null ? "The payment was declined" : saved.getFailureReason());
                }
            }
        } catch (PaymentFieldException ex) {
            out.put("ok", false);
            out.put("status", "INVALID");
            out.put("message", "Please correct the highlighted details.");
            out.put("errors", ex.getErrors());
        } catch (BusinessRuleException ex) {
            out.put("ok", false);
            out.put("status", "FAILED");
            out.put("message", ex.getMessage());
        }
        return out;
    }

    // -------------------------------------------------------------- history

    @GetMapping("/payments")
    public String history(HttpSession session, Model model) {
        model.addAttribute("payments", payments.forCustomer(Auth.userId(session)));
        return "payment/payment-history";
    }

    // ------------------------------------------------------------- receipts

    @GetMapping("/payments/{id}/receipt")
    public String receipt(@PathVariable Long id, @RequestParam(name = "paid", required = false) String paid,
                          HttpSession session, Model model) {
        Payment payment = owned(id, session);
        model.addAttribute("receipt", payments.receiptOf(payment));
        model.addAttribute("paymentId", payment.getId());
        model.addAttribute("adminView", false);
        model.addAttribute("justPaid", paid != null);
        return "payment/receipt";
    }

    @GetMapping("/payments/{id}/receipt.pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable Long id, HttpSession session) {
        return pdf(owned(id, session));
    }

    private Payment owned(Long id, HttpSession session) {
        Payment payment = payments.findById(id);
        bookings.findForCustomer(payment.getBookingId(), Auth.userId(session));   // 404 if it is not theirs
        return payment;
    }

    /** Shared with the admin controller. */
    public static ResponseEntity<byte[]> pdfResponse(byte[] bytes, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(bytes);
    }

    private ResponseEntity<byte[]> pdf(Payment payment) {
        return pdfResponse(payments.receiptPdf(payment), payments.receiptFileName(payment));
    }

    // ------------------------------------------------------------- internals

    private void fill(Model model, Booking booking) {
        boolean fine = booking.getStatus() == BookingStatus.FINE_DUE;
        model.addAttribute("booking", booking);
        model.addAttribute("fine", fine);
        model.addAttribute("amountDue", fine ? booking.getFineAmount() : booking.getTotalAmount());
        List<PaymentMethod> methods = fine
                ? List.of(PaymentMethod.CARD, PaymentMethod.WALLET)
                : List.of(PaymentMethod.CARD, PaymentMethod.WALLET, PaymentMethod.BANK_TRANSFER, PaymentMethod.CASH);
        model.addAttribute("methods", methods);
        model.addAttribute("walletProviders", WalletPaymentProcessor.PROVIDERS);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAccountName", bankAccountName);
        model.addAttribute("bankAccountNumber", bankAccountNumber);
        model.addAttribute("bankBranch", bankBranch);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("minTransferDate", LocalDate.now().minusDays(7));
        if (!model.containsAttribute("fieldErrors")) {
            model.addAttribute("fieldErrors", Map.of());
        }
    }
}
