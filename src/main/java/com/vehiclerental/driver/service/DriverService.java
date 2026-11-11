package com.vehiclerental.driver.service;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingCancelledEvent;
import com.vehiclerental.booking.service.BookingConfirmedEvent;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.driver.dto.DriverForm;
import com.vehiclerental.driver.entity.AssignmentStatus;
import com.vehiclerental.driver.entity.Driver;
import com.vehiclerental.driver.entity.DriverAssignment;
import com.vehiclerental.driver.entity.DriverStatus;
import com.vehiclerental.driver.repository.DriverAssignmentRepository;
import com.vehiclerental.driver.repository.DriverRepository;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.notification.EmailService;
import com.vehiclerental.notification.EmailTemplates;
import com.vehiclerental.notification.EmailType;
import com.vehiclerental.notification.NotificationService;
import com.vehiclerental.notification.NotificationType;
import com.vehiclerental.user.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Drivers for "with driver" bookings: CRUD, finding a free driver for the dates, assigning one when a booking
 * is confirmed, and telling everybody (the driver by email with a trip link, the customer, the admins).
 *
 * It listens to BookingConfirmedEvent / BookingCancelledEvent so BookingService never needs to know about drivers.
 */
@Service
public class DriverService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final DriverRepository drivers;
    private final DriverAssignmentRepository assignments;
    private final BookingService bookings;
    private final UserService users;
    private final EmailService email;
    private final EmailTemplates templates;
    private final NotificationService notifications;

    public DriverService(DriverRepository drivers, DriverAssignmentRepository assignments, @Lazy BookingService bookings,
                         UserService users, EmailService email, EmailTemplates templates,
                         NotificationService notifications) {
        this.drivers = drivers;
        this.assignments = assignments;
        this.bookings = bookings;
        this.users = users;
        this.email = email;
        this.templates = templates;
        this.notifications = notifications;
    }

    // ================================================================ CRUD

    public Driver create(DriverForm form) {
        Driver d = new Driver();
        copy(form, d);
        return drivers.create(d);
    }

    public Driver findById(Long id) {
        return drivers.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Driver", id));
    }

    public List<Driver> findAll() {
        return drivers.findAll().stream().sorted(Comparator.comparing(Driver::getFullName, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public Driver update(Long id, DriverForm form) {
        Driver d = findById(id);
        copy(form, d);
        return drivers.update(d);
    }

    public void delete(Long id) {
        findById(id);
        boolean busy = assignments.findByDriverId(id).stream().anyMatch(a -> a.isLive()
                && !a.getReturnDate().isBefore(LocalDate.now()));
        if (busy) {
            throw new BusinessRuleException("This driver has upcoming trips. Set them to off duty instead of deleting.");
        }
        drivers.deleteById(id);
    }

    public long count() {
        return drivers.count();
    }

    private static void copy(DriverForm f, Driver d) {
        d.setFullName(f.getFullName());
        d.setPhone(f.getPhone());
        d.setEmail(f.getEmail());
        d.setLicenceNo(f.getLicenceNo());
        d.setLanguages(f.getLanguages());
        d.setStatus(f.getStatus());
    }

    // ============================================================ Availability

    /** Drivers on duty who have no live trip overlapping the dates. */
    public List<Driver> findFree(LocalDate from, LocalDate to) {
        return findAll().stream().filter(Driver::isAssignable).filter(d -> isFree(d.getId(), from, to, null)).toList();
    }

    private boolean isFree(Long driverId, LocalDate from, LocalDate to, Long ignoreBookingId) {
        return assignments.findByDriverId(driverId).stream()
                .filter(DriverAssignment::isLive)
                .filter(a -> ignoreBookingId == null || !ignoreBookingId.equals(a.getBookingId()))
                .noneMatch(a -> !a.getPickupDate().isAfter(to) && !a.getReturnDate().isBefore(from));
    }

    public List<DriverAssignment> assignmentsOf(Long driverId) {
        return assignments.findByDriverId(driverId).stream()
                .sorted(Comparator.comparing(DriverAssignment::getPickupDate).reversed()).toList();
    }

    public DriverAssignment liveAssignmentFor(Long bookingId) {
        return assignments.findByBookingId(bookingId).stream().filter(DriverAssignment::isLive).findFirst().orElse(null);
    }

    /** Number of confirmed with-driver bookings still waiting for a driver (shown on the admin dashboard). */
    public long countUnassigned() {
        return bookings.search(null, null).stream().filter(Booking::isDriverPending)
                .filter(b -> b.getStatus() == com.vehiclerental.booking.entity.BookingStatus.CONFIRMED).count();
    }

    // ============================================================== Assigning

    /** Booking confirmed: try to pick a free driver automatically. If none is free the admin is alerted. */
    @EventListener
    public void onConfirmed(BookingConfirmedEvent event) {
        Booking booking = bookings.findById(event.getBookingId());
        if (!booking.isWithDriver() || booking.getDriverId() != null) {
            return;
        }
        List<Driver> free = findFree(booking.getPickupDate(), booking.getReturnDate());
        Long wanted = booking.getPreferredDriverId();
        Driver chosen = null;
        if (wanted != null) {
            chosen = free.stream().filter(d -> wanted.equals(d.getId())).findFirst().orElse(null);
        }
        boolean notThePreferred = wanted != null && chosen == null;
        if (chosen == null && !free.isEmpty()) {
            chosen = free.get(0);
        }
        if (chosen == null) {
            notifications.notifyAdmins(NotificationType.DRIVER, "No driver free for " + booking.getReference(),
                    "Booking " + booking.getReference() + " needs a driver from " + booking.getPickupDate().format(DATE)
                            + " to " + booking.getReturnDate().format(DATE) + " but nobody is free. Assign one manually.",
                    "/admin/bookings/" + booking.getId());
            return;
        }
        assign(booking.getId(), chosen.getId());
        if (notThePreferred) {
            String asked = drivers.findById(wanted).map(Driver::getFullName).orElse("the requested driver");
            notifications.notifyAdmins(NotificationType.DRIVER, "Preferred driver not free for " + booking.getReference(),
                    "The customer asked for " + asked + " but they are not free on those dates, so "
                            + chosen.getFullName() + " was assigned. You can change the driver on the booking page.",
                    "/admin/bookings/" + booking.getId());
        }
    }

    /** Assign (or re-assign) a driver to a booking; notifies the driver, the customer and the admins. */
    public DriverAssignment assign(Long bookingId, Long driverId) {
        Booking booking = bookings.findById(bookingId);
        Driver driver = findById(driverId);
        if (!booking.isWithDriver()) {
            throw new BusinessRuleException("This booking is self drive.");
        }
        if (!driver.isAssignable()) {
            throw new BusinessRuleException(driver.getFullName() + " is off duty.");
        }
        if (!isFree(driverId, booking.getPickupDate(), booking.getReturnDate(), bookingId)) {
            throw new BusinessRuleException(driver.getFullName() + " already has a trip on those dates.");
        }
        List<DriverAssignment> replaced = new java.util.ArrayList<>();
        for (DriverAssignment old : assignments.findByBookingId(bookingId)) {
            if (old.isLive()) {
                old.setStatus(AssignmentStatus.CANCELLED);
                assignments.update(old);
                replaced.add(old);
            }
        }
        DriverAssignment a = new DriverAssignment();
        a.setBookingId(bookingId);
        a.setDriverId(driverId);
        a.setPickupDate(booking.getPickupDate());
        a.setReturnDate(booking.getReturnDate());
        a.setStatus(AssignmentStatus.ASSIGNED);
        a.setToken(UUID.randomUUID().toString().replace("-", ""));
        a.setNotifiedAt(LocalDateTime.now());
        a = assignments.create(a);

        bookings.assignDriver(bookingId, driver.getId(), driver.getFullName(), driver.getPhone());
        for (DriverAssignment old : replaced) {
            if (!driverId.equals(old.getDriverId())) {
                drivers.findById(old.getDriverId()).ifPresent(previous -> notifyRemoved(booking, previous));
            }
        }
        notifyDriver(booking, driver, a);
        notifyCustomer(booking, driver);
        notifications.notifyAdmins(NotificationType.DRIVER, driver.getFullName() + " assigned to " + booking.getReference(),
                driver.getFullName() + " will drive booking " + booking.getReference() + " ("
                        + booking.getPickupDate().format(DATE) + " to " + booking.getReturnDate().format(DATE) + ").",
                "/admin/bookings/" + bookingId);
        return a;
    }

    private void notifyDriver(Booking b, Driver driver, DriverAssignment a) {
        if (driver.getEmail() == null || driver.getEmail().isBlank()) {
            return;
        }
        var mail = templates.builder("New trip: " + b.getReference())
                .greeting("Hello " + driver.getFullName() + ",")
                .paragraph("You have been assigned a new trip. Please confirm that you have seen it using the button below.")
                .highlight("Trip", b.getReference())
                .fact("Vehicle", b.getVehicleModel() + " (" + b.getVehicleRegNo() + ")")
                .fact("Dates", b.getPickupDate().format(DATE) + " to " + b.getReturnDate().format(DATE))
                .fact("Pickup", b.getPickupLocationName() + " at " + b.getPickupTime())
                .fact("Drop-off", b.getDropLocationName())
                .fact("Customer", b.getRenterName());
        if (b.getPickupAddress() != null && !b.getPickupAddress().isBlank()) {
            mail = mail.fact("Collect customer at", b.getPickupAddress());
        }
        if (b.getDriverNote() != null) {
            mail = mail.fact("Note from the customer", b.getDriverNote());
        }
        email.send(mail.button("View trip and confirm", "/driver/trip/" + a.getToken())
                .build(driver.getEmail(), EmailType.DRIVER));
    }

    /** The booking was moved to another driver: tell the one who is no longer needed. */
    private void notifyRemoved(Booking b, Driver previous) {
        if (previous.getEmail() == null || previous.getEmail().isBlank()) {
            return;
        }
        email.send(templates.builder("Trip reassigned: " + b.getReference())
                .greeting("Hello " + previous.getFullName() + ",")
                .paragraph("The trip below is no longer assigned to you. You are free on those dates and nothing more is needed from you.")
                .highlight("Trip", b.getReference())
                .fact("Vehicle", b.getVehicleModel() + " (" + b.getVehicleRegNo() + ")")
                .fact("Dates", b.getPickupDate().format(DATE) + " to " + b.getReturnDate().format(DATE))
                .build(previous.getEmail(), EmailType.DRIVER));
    }

    private void notifyCustomer(Booking b, Driver driver) {
        notifications.notifyCustomer(b.getCustomerId(), NotificationType.DRIVER, "Your driver is ready",
                driver.getFullName() + " (" + driver.getPhone() + ") will drive you on booking " + b.getReference() + ".",
                "/bookings/" + b.getId());
        email.send(templates.builder("Your driver for " + b.getReference())
                .greeting("Hello " + b.getRenterName() + ",")
                .paragraph("A driver has been assigned to your booking.")
                .highlight("Driver", driver.getFullName())
                .fact("Phone", driver.getPhone())
                .fact("Languages", driver.getLanguages())
                .fact("Pickup", b.getPickupLocationName() + " at " + b.getPickupTime())
                .button("View my booking", "/bookings/" + b.getId())
                .build(users.findById(b.getCustomerId()).getEmail(), EmailType.DRIVER));
    }

    // ============================================================ Cancelling

    @EventListener
    public void onCancelled(BookingCancelledEvent event) {
        for (DriverAssignment a : assignments.findByBookingId(event.getBookingId())) {
            if (a.isLive()) {
                a.setStatus(AssignmentStatus.CANCELLED);
                assignments.update(a);
                drivers.findById(a.getDriverId()).ifPresent(d -> {
                    if (d.getEmail() != null && !d.getEmail().isBlank()) {
                        email.send(templates.builder("Trip cancelled")
                                .greeting("Hello " + d.getFullName() + ",")
                                .paragraph("A trip you were assigned (" + a.getPickupDate().format(DATE) + " to "
                                        + a.getReturnDate().format(DATE) + ") has been cancelled. You are free on those dates.")
                                .build(d.getEmail(), EmailType.DRIVER));
                    }
                });
            }
        }
    }

    // =================================================== Driver's trip page

    public DriverAssignment findByToken(String token) {
        return assignments.findByToken(token).orElseThrow(() -> ResourceNotFoundException.of("Trip", 0L));
    }

    public Booking bookingOf(DriverAssignment a) {
        return bookings.findById(a.getBookingId());
    }

    public Driver driverOf(DriverAssignment a) {
        return findById(a.getDriverId());
    }

    /** The driver pressed "I have seen this trip". */
    public void acknowledge(String token) {
        DriverAssignment a = findByToken(token);
        if (a.getStatus() == AssignmentStatus.ASSIGNED) {
            a.setStatus(AssignmentStatus.ACKNOWLEDGED);
            a.setAcknowledgedAt(LocalDateTime.now());
            assignments.update(a);
            Booking b = bookingOf(a);
            notifications.notifyAdmins(NotificationType.DRIVER, driverOf(a).getFullName() + " confirmed the trip",
                    driverOf(a).getFullName() + " acknowledged booking " + b.getReference() + ".",
                    "/admin/bookings/" + b.getId());
        }
    }
}
