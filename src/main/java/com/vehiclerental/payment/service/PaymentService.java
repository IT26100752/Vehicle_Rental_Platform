package com.vehiclerental.payment.service;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.common.service.AbstractCrudService;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MEMBER 4 - Payment Management service.
 * Payments are SIMULATED (CARD / CASH / ONLINE) as the intro session allows;
 * no real gateway is involved.
 */
@Service
public class PaymentService extends AbstractCrudService<Payment> {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    public PaymentService(PaymentRepository paymentRepository, BookingService bookingService) {
        super(paymentRepository);
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    /* ---------------------------- CREATE ---------------------------- */
    /**
     * Creates a PENDING payment for a booking.
     * Amount = booking total + late fine (if any).
     */
    public Payment createForBooking(int bookingId, PaymentMethod method) {
        if (paymentRepository.findByBookingId(bookingId).isPresent()) {
            throw new IllegalStateException("A payment already exists for booking #" + bookingId);
        }
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        double amount = booking.getTotalAmount() + booking.getFine();
        String reference = "TXN-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

        return save(new Payment(0, bookingId, amount, method,
                LocalDate.now(), PaymentStatus.PENDING, reference));
    }

    /* ---------------------------- UPDATE ---------------------------- */
    public Payment updateStatus(int paymentId, PaymentStatus newStatus) {
        Payment payment = findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found"));
        payment.setStatus(newStatus);
        payment.setPaymentDate(LocalDate.now());
        update(payment);
        return payment;
    }

    /* ---------------------------- READ ------------------------------ */
    public Optional<Payment> findByBookingId(int bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> history() {
        return paymentRepository.findAllNewestFirst();
    }

    public List<Payment> historyForBookings(List<Integer> bookingIds) {
        return paymentRepository.findByBookingIds(bookingIds);
    }

    public double revenue() {
        return paymentRepository.totalRevenue();
    }
}
