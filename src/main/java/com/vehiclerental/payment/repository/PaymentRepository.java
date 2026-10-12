package com.vehiclerental.payment.repository;

import com.vehiclerental.common.repository.AbstractFileRepository;
import com.vehiclerental.common.repository.DataStore;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentStatus;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Member 4's repository - everything about the payments storage.
 */
@Repository
public class PaymentRepository extends AbstractFileRepository<Payment> {

    public PaymentRepository(DataStore<Payment> store) {
        super(store);
    }

    public Optional<Payment> findByBookingId(int bookingId) {
        return findAll().stream()
                .filter(p -> p.getBookingId() == bookingId)
                .findFirst();
    }

    /** Payments whose booking belongs to the given user (via bookings list). */
    public List<Payment> findByBookingIds(List<Integer> bookingIds) {
        return findAll().stream()
                .filter(p -> bookingIds.contains(p.getBookingId()))
                .sorted(Comparator.comparing(Payment::getId).reversed())
                .toList();
    }

    public List<Payment> findAllNewestFirst() {
        return findAll().stream()
                .sorted(Comparator.comparing(Payment::getId).reversed())
                .toList();
    }

    /** Total income = sum of COMPLETED payments (dashboard stat card). */
    public double totalRevenue() {
        return findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}
