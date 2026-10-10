package com.vehiclerental.booking.repository;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.common.repository.AbstractFileRepository;
import com.vehiclerental.common.repository.DataStore;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Member 3's repository - everything about the bookings storage.
 */
@Repository
public class BookingRepository extends AbstractFileRepository<Booking> {

    public BookingRepository(DataStore<Booking> store) {
        super(store);
    }

    public List<Booking> findByUserId(int userId) {
        return findAll().stream()
                .filter(b -> b.getUserId() == userId)
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .toList();
    }

    public List<Booking> findByVehicleId(int vehicleId) {
        return findAll().stream()
                .filter(b -> b.getVehicleId() == vehicleId)
                .toList();
    }

    public Optional<Booking> findActiveByVehicleId(int vehicleId) {
        return findAll().stream()
                .filter(b -> b.getVehicleId() == vehicleId && b.isActive())
                .findFirst();
    }

    public List<Booking> findByStatus(BookingStatus status) {
        return findAll().stream()
                .filter(b -> b.getStatus() == status)
                .toList();
    }

    /** Newest first - used by the admin dashboard "recent bookings" table. */
    public List<Booking> findRecent(int limit) {
        return findAll().stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .limit(limit)
                .toList();
    }
}
