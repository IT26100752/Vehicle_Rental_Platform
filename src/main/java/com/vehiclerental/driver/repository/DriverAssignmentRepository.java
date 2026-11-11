package com.vehiclerental.driver.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.driver.entity.AssignmentStatus;
import com.vehiclerental.driver.entity.DriverAssignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Stores driver assignments in data/driver_assignments.txt */
@Repository
public class DriverAssignmentRepository extends FileRepository<DriverAssignment> {

    public DriverAssignmentRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "driver_assignments.txt"));
    }

    public List<DriverAssignment> findByBookingId(Long bookingId) {
        return findWhere(a -> bookingId.equals(a.getBookingId()));
    }

    public List<DriverAssignment> findByDriverId(Long driverId) {
        return findWhere(a -> driverId.equals(a.getDriverId()));
    }

    public Optional<DriverAssignment> findByToken(String token) {
        return token == null ? Optional.empty() : findFirst(a -> token.equals(a.getToken()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|bookingId|driverId|pickupDate|returnDate|status|token|notifiedAt|acknowledgedAt";
    }

    @Override
    protected String[] toFields(DriverAssignment a) {
        return new String[]{text(a.getBookingId()), text(a.getDriverId()), text(a.getPickupDate()),
                text(a.getReturnDate()), a.getStatus().name(), text(a.getToken()), text(a.getNotifiedAt()),
                text(a.getAcknowledgedAt())};
    }

    @Override
    protected DriverAssignment fromFields(String[] f) {
        DriverAssignment a = new DriverAssignment();
        a.setBookingId(lng(f, 0));
        a.setDriverId(lng(f, 1));
        a.setPickupDate(date(f, 2));
        a.setReturnDate(date(f, 3));
        a.setStatus(enumOf(AssignmentStatus.class, f, 4, AssignmentStatus.ASSIGNED));
        a.setToken(strOrNull(f, 5));
        a.setNotifiedAt(dateTime(f, 6));
        a.setAcknowledgedAt(dateTime(f, 7));
        return a;
    }
}
