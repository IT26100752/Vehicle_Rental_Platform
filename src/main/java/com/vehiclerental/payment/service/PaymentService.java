package com.vehiclerental.payment.service;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.service.BookingCancelledEvent;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.notification.EmailService;
import com.vehiclerental.notification.EmailTemplates;
import com.vehiclerental.notification.EmailType;
import com.vehiclerental.notification.NotificationService;
import com.vehiclerental.notification.NotificationType;
import com.vehiclerental.payment.dto.PaymentForm;
import com.vehiclerental.payment.entity.CashPayment;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.processor.PaymentProcessor;
import com.vehiclerental.payment.repository.PaymentRepository;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Payments for rentals and fines: online methods (card, wallet, bank transfer - each handled by its own
 * {@link PaymentProcessor}), cash at the counter, admin verification of bank transfers, refunds, receipts
 * and revenue figures.
 */
@Service
public class PaymentService {

    private static final java.time.format.DateTimeFormatter DATE = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final PaymentRepository repository;
    private final BookingService bookings;
    private final UserService users;
    private final EmailService email;
    private final EmailTemplates templates;
    private final NotificationService notifications;
    private final ReceiptService receipts;
    private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);

    public PaymentService(PaymentRepository repository, BookingService bookings, UserService users, EmailService email,
                          EmailTemplates templates, NotificationService notifications, ReceiptService receipts,
                          List<PaymentProcessor> processorList) {
        this.repository = repository;
        this.bookings = bookings;
        this.users = users;
        this.email = email;
        this.templates = templates;
        this.notifications = notifications;
        this.receipts = receipts;
        for (PaymentProcessor p : processorList) {
            processors.put(p.method(), p);
        }
    }

    // ================================================================ Create

    /**
     * The customer pays the rental (booking waiting for payment) or the late fine (booking with a fine due).
     * The result's status tells what happened: PAID, PENDING_VERIFICATION (bank transfer) or FAILED (declined).
     * Invalid details throw PaymentFieldException and nothing is saved.
     */
    public Payment pay(Long customerId, Long bookingId, PaymentForm form) {
        Booking booking = bookings.findForCustomer(bookingId, customerId);
        boolean fine = booking.getStatus() == BookingStatus.FINE_DUE;
        if (!fine && booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("This booking is not waiting for payment");
        }
        PaymentProcessor processor = processors.get(form.getMethod());
        if (processor == null || !form.getMethod().isOnline()) {
            throw new BusinessRuleException("That payment method is not available online");
        }
        if (fine && !processor.supportsFines()) {
            throw new BusinessRuleException("A late fine must be paid by card or wallet");
        }
        BigDecimal amount = fine ? booking.getFineAmount() : booking.getTotalAmount();
        PaymentKind kind = fine ? PaymentKind.FINE : PaymentKind.RENTAL;
        Payment payment = processor.process(booking, form, amount, kind);   // may throw PaymentFieldException

        switch (payment.getStatus()) {
            case FAILED -> {
                Payment saved = repository.create(payment);
                notifications.notifyCustomer(customerId, NotificationType.PAYMENT, "Payment failed",
                        saved.getFailureReason() + ". Your booking " + booking.getReference() + " is still waiting.",
                        "/payments/booking/" + bookingId);
                return saved;
            }
            case PENDING_VERIFICATION -> {
                bookings.markAwaitingVerification(bookingId);       // fails if the payment window expired
                Payment saved = repository.create(payment);
                onTransferSubmitted(bookings.findById(bookingId), saved);
                return saved;
            }
            default -> {
                if (fine) {
                    Payment saved = savePaid(payment);
                    bookings.completeAfterFine(bookingId);
                    onFinePaid(booking, saved);
                    return saved;
                }
                bookings.markPaid(bookingId);                       // fails (and cancels) if the window expired
                Payment saved = savePaid(payment);
                onRentalPaid(bookings.findById(bookingId), saved);
                return saved;
            }
        }
    }

    /** Admin takes cash at the counter: either the rental (confirms the booking) or the late fine. */
    public Payment recordCash(Long bookingId, String adminName) {
        Booking booking = bookings.findById(bookingId);
        CashPayment payment = new CashPayment();
        payment.setBookingId(bookingId);
        payment.setCustomerId(booking.getCustomerId());
        payment.setReceivedBy(adminName);
        payment.setReference(newReference());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            payment.setKind(PaymentKind.RENTAL);
            payment.setAmount(booking.getTotalAmount());
            bookings.markPaid(bookingId);
            Payment saved = savePaid(payment);
            onRentalPaid(bookings.findById(bookingId), saved);
            return saved;
        }
        if (booking.getStatus() == BookingStatus.FINE_DUE) {
            payment.setKind(PaymentKind.FINE);
            payment.setAmount(booking.getFineAmount());
            Payment saved = savePaid(payment);
            bookings.completeAfterFine(bookingId);
            onFinePaid(booking, saved);
            return saved;
        }
        throw new BusinessRuleException("Nothing is due on this booking");
    }

    // ------------------------------------------------- bank transfer review

    /** Admin saw the money in the bank account: the payment is PAID and the booking is CONFIRMED. */
    public Payment approve(Long paymentId, String adminName) {
        Payment payment = pendingTransfer(paymentId);
        bookings.confirmVerified(payment.getBookingId());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setVerifiedBy(adminName);
        if (payment instanceof CashPayment cash) {
            cash.setReceivedBy(adminName);
        }
        Payment saved = savePaid(payment);
        onRentalPaid(bookings.findById(saved.getBookingId()), saved);
        return saved;
    }

    /** Admin could not find the money: the customer is told why and can try another method. */
    public Payment reject(Long paymentId, String adminName, String reason) {
        Payment payment = pendingTransfer(paymentId);
        boolean cash = payment.getMethodType() == PaymentMethod.CASH;
        String why = reason == null || reason.isBlank()
                ? (cash ? "The cash was not received at the counter" : "We could not find this transfer in our bank account")
                : reason.trim();
        bookings.rejectVerification(payment.getBookingId());
        payment.setStatus(PaymentStatus.REJECTED);
        payment.setFailureReason(why);
        payment.setVerifiedBy(adminName);
        Payment saved = repository.update(payment);
        Booking booking = bookings.findById(saved.getBookingId());
        notifications.notifyCustomer(booking.getCustomerId(), NotificationType.PAYMENT, cash ? "Cash payment not received" : "Bank transfer rejected",
                why + ". Please pay again for booking " + booking.getReference() + ".", "/payments/booking/" + booking.getId());
        email.send(templates.builder(cash ? "Cash payment not received" : "Bank transfer not verified")
                .greeting("Hello " + booking.getRenterName() + ",")
                .paragraph((cash ? "We did not receive your cash payment for booking " : "We could not verify your bank transfer for booking ") + booking.getReference() + ".")
                .highlight("Reason", why)
                .paragraph("Your booking is still held for a few more minutes. Please pay again using another method.")
                .button("Pay again", "/payments/booking/" + booking.getId())
                .build(users.findById(booking.getCustomerId()).getEmail(), EmailType.PAYMENT));
        return saved;
    }

    private Payment pendingTransfer(Long paymentId) {
        Payment payment = findById(paymentId);
        if (payment.getStatus() != PaymentStatus.PENDING_VERIFICATION) {
            throw new BusinessRuleException("This payment is not waiting for verification");
        }
        return payment;
    }

    private Payment savePaid(Payment payment) {
        Payment saved = payment.getId() == null ? repository.create(payment) : repository.update(payment);
        saved.setReceiptNo(ReceiptService.receiptNumberFor(saved));
        return repository.update(saved);
    }

    // ================================================================== Read

    public Payment findById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    public List<Payment> forCustomer(Long customerId) {
        return repository.findByCustomerId(customerId).stream().sorted(newestFirst()).toList();
    }

    public List<Payment> forBooking(Long bookingId) {
        return repository.findByBookingId(bookingId).stream().sorted(newestFirst()).toList();
    }

    public Map<PaymentStatus, Long> countByStatus() {
        Map<PaymentStatus, Long> counts = new EnumMap<>(PaymentStatus.class);
        for (PaymentStatus st : PaymentStatus.values()) {
            counts.put(st, 0L);
        }
        for (Payment p : repository.findAll()) {
            counts.merge(p.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    public long countPendingVerification() {
        return repository.findAll().stream().filter(p -> p.getStatus() == PaymentStatus.PENDING_VERIFICATION).count();
    }

    public byte[] receiptPdf(Payment p) {
        return receipts.pdf(p);
    }

    public String receiptFileName(Payment p) {
        return receipts.fileName(p);
    }

    public com.vehiclerental.payment.receipt.Receipt receiptOf(Payment p) {
        return receipts.build(p);
    }

    public List<Payment> search(String keyword, PaymentStatus status) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findAll().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> k.isEmpty()
                        || (p.getReference() != null && p.getReference().toLowerCase().contains(k))
                        || (p.getReceiptNo() != null && p.getReceiptNo().toLowerCase().contains(k))
                        || String.valueOf(p.getBookingId()).equals(k)
                        || p.getMethod().toLowerCase().contains(k))
                .sorted(newestFirst()).toList();
    }

    // -------------------------------------------------------------- revenue

    public BigDecimal totalRevenue() {
        return sum(repository.findAll().stream().filter(this::counts).toList());
    }

    public BigDecimal revenueOn(LocalDate day) {
        return sum(repository.findAll().stream()
                .filter(this::counts).filter(p -> p.getPaidAt().toLocalDate().equals(day)).toList());
    }

    public BigDecimal revenueIn(YearMonth month) {
        return sum(repository.findAll().stream()
                .filter(this::counts).filter(p -> YearMonth.from(p.getPaidAt()).equals(month)).toList());
    }

    /** Money from rentals that are fully COMPLETED. */
    public BigDecimal revenueFromCompletedRentals() {
        Set<Long> completed = bookings.idsWithStatus(BookingStatus.COMPLETED);
        return sum(repository.findAll().stream()
                .filter(this::counts).filter(p -> completed.contains(p.getBookingId())).toList());
    }

    // ================================================================ Update

    /** A cancelled booking gets its rental payment refunded (simulated); a transfer still being checked is closed. */
    @EventListener
    public void onBookingCancelled(BookingCancelledEvent event) {
        BigDecimal refunded = BigDecimal.ZERO;
        for (Payment payment : repository.findByBookingId(event.getBookingId())) {
            if (payment.getStatus() == PaymentStatus.PAID && payment.getKind() == PaymentKind.RENTAL) {
                payment.setStatus(PaymentStatus.REFUNDED);
                repository.update(payment);
                refunded = refunded.add(payment.getAmount());
            } else if (payment.getStatus() == PaymentStatus.PENDING_VERIFICATION) {
                payment.setStatus(PaymentStatus.REJECTED);
                payment.setFailureReason("Booking cancelled");
                repository.update(payment);
            }
        }
        if (refunded.signum() > 0) {
            Booking booking = bookings.findById(event.getBookingId());
            Customer customer = users.findById(booking.getCustomerId());
            email.send(templates.builder("Booking " + booking.getReference() + " cancelled")
                    .greeting("Hello " + customer.getFullName() + ",")
                    .paragraph("Your booking for the " + booking.getVehicleModel() + " has been cancelled.")
                    .highlight("Refund", "LKR " + money(refunded))
                    .paragraph("The refund will be returned to your original payment method.")
                    .build(customer.getEmail(), EmailType.PAYMENT));
            notifications.notifyCustomer(customer.getId(), NotificationType.PAYMENT, "Refund issued",
                    "LKR " + money(refunded) + " refunded for booking " + booking.getReference() + ".", "/payments");
        }
    }

    // ================================================================ Delete

    /** Admin housekeeping: remove a payment record. */
    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw ResourceNotFoundException.of("Payment", id);
        }
    }

    // ============================================================== internals

    /** Rental paid (online, cash or approved transfer): email the PDF receipt, tell the customer and the admins. */
    private void onRentalPaid(Booking booking, Payment payment) {
        Customer customer = users.findById(booking.getCustomerId());
        email.send(templates.builder("Booking confirmed - " + booking.getReference())
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("Thank you! Your payment was received and your booking is confirmed. Your receipt is attached as a PDF.")
                .highlight("Booking", booking.getReference())
                .fact("Vehicle", booking.getVehicleModel() + " (" + booking.getVehicleRegNo() + ")")
                .fact("Travel", booking.isWithDriver() ? "With a driver" : "Self drive")
                .fact("Pickup", booking.getPickupDate().format(DATE) + " at " + booking.getPickupLocationName()
                        + (booking.getPickupTime() == null ? "" : ", " + booking.getPickupTime()))
                .fact("Drop-off", booking.getReturnDate().format(DATE) + " at " + booking.getDropLocationName())
                .fact("Amount paid", "LKR " + money(payment.getAmount()))
                .fact("Method", payment.getMethodDetails())
                .fact("Receipt no", payment.getReceiptNo())
                .note("At pickup bring your ORIGINAL NIC" + (booking.isWithDriver() ? "." : " and ORIGINAL driving licence.")
                        + " Late returns are charged " + bookings.lateFeePolicyFor(customer.getTier()).describe() + ".")
                .button("View my booking", "/bookings/" + booking.getId())
                .build(customer.getEmail(), EmailType.RECEIPT)
                .withAttachment(receipts.attachment(payment)));
        notifications.notifyCustomer(customer.getId(), NotificationType.PAYMENT, "Payment received",
                "Booking " + booking.getReference() + " is confirmed. Your receipt is ready.",
                "/payments/" + payment.getId() + "/receipt");
        notifications.notifyAdmins(NotificationType.BOOKING, "New confirmed booking " + booking.getReference(),
                customer.getFullName() + " paid LKR " + money(payment.getAmount()) + " by " + payment.getMethod().toLowerCase()
                        + " for the " + booking.getVehicleModel() + (booking.isWithDriver() ? " (with driver)." : "."),
                "/admin/bookings/" + booking.getId());
    }

    /** Bank transfer submitted: tell the customer it is being checked, and ask the admins to verify it. */
    private void onTransferSubmitted(Booking booking, Payment payment) {
        Customer customer = users.findById(booking.getCustomerId());
        boolean cash = payment.getMethodType() == PaymentMethod.CASH;
        email.send(templates.builder(cash ? "Your cash payment is reserved - " + booking.getReference()
                        : "We received your transfer details - " + booking.getReference())
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph(cash
                        ? "Thank you. Your vehicle is held for you. Please pay LKR " + money(payment.getAmount())
                                + " in cash at our counter; your booking is confirmed and your receipt is emailed as soon as our staff receive the money."
                        : "Thank you. We are checking our bank account for your transfer. Your vehicle is held for you meanwhile, "
                                + "and you will get your receipt by email as soon as it is confirmed.")
                .highlight("Amount", "LKR " + money(payment.getAmount()))
                .fact(cash ? "Method" : "Bank", payment.getMethodDetails())
                .fact("Reference", payment.getReference())
                .button("View my booking", "/bookings/" + booking.getId())
                .build(customer.getEmail(), EmailType.PAYMENT));
        notifications.notifyAdmins(NotificationType.PAYMENT,
                (cash ? "Cash payment to confirm (" : "Bank transfer to verify (") + booking.getReference() + ")",
                cash ? customer.getFullName() + " will pay LKR " + money(payment.getAmount())
                        + " in cash at the counter. Confirm it once the money is received."
                     : customer.getFullName() + " says they transferred LKR " + money(payment.getAmount())
                        + " (" + payment.getMethodDetails() + "). Please check the bank account and approve or reject it.",
                "/admin/payments?status=PENDING_VERIFICATION");
    }

    private void onFinePaid(Booking booking, Payment payment) {
        Customer customer = users.findById(booking.getCustomerId());
        email.send(templates.builder("Late fine received - " + booking.getReference())
                .greeting("Hello " + customer.getFullName() + ",")
                .paragraph("We received your payment for the late return of the " + booking.getVehicleModel() + " ("
                        + booking.getLateDays() + " day(s) late). Booking " + booking.getReference() + " is now completed. "
                        + "Your receipt is attached.")
                .highlight("Fine paid", "LKR " + money(payment.getAmount()))
                .build(customer.getEmail(), EmailType.RECEIPT)
                .withAttachment(receipts.attachment(payment)));
        notifications.notifyCustomer(customer.getId(), NotificationType.PAYMENT, "Fine received",
                "Booking " + booking.getReference() + " is completed.", "/payments/" + payment.getId() + "/receipt");
    }

    private boolean counts(Payment p) {
        return p.getStatus() == PaymentStatus.PAID;
    }

    private static BigDecimal sum(List<Payment> payments) {
        return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Comparator<Payment> newestFirst() {
        return Comparator.comparing(Payment::getCreatedAt).reversed();
    }

    private static String newReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String money(BigDecimal amount) {
        return String.format("%,.2f", amount);
    }
}
