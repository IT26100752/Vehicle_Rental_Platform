package com.vehiclerental.booking.service;

import com.vehiclerental.booking.dto.Availability;
import com.vehiclerental.booking.dto.BookingForm;
import com.vehiclerental.booking.dto.ReturnForm;
import com.vehiclerental.booking.dto.VehicleRentalCount;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.pricing.PriceQuote;
import com.vehiclerental.booking.pricing.PricingService;
import com.vehiclerental.location.entity.Location;
import com.vehiclerental.promo.entity.PromoCode;
import com.vehiclerental.promo.service.PromoService;
import com.vehiclerental.booking.pricing.PriceLine;
import com.vehiclerental.location.service.LocationService;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.CustomerTier;
import com.vehiclerental.booking.repository.BookingRepository;
import com.vehiclerental.common.DateRules;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.user.service.UserService;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.service.VehicleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The heart of the system: availability, booking life-cycle and late-return fines.
 *
 * PENDING_PAYMENT -> CONFIRMED -> ACTIVE -> COMPLETED
 *                                      \-> FINE_DUE -> COMPLETED
 * (a booking can be CANCELLED while PENDING_PAYMENT, CONFIRMED or - by an admin - ACTIVE)
 */
@Service
public class BookingService {

    public static final int MAX_RENTAL_DAYS = 60;

    private final BookingRepository repository;
    private final VehicleService vehicles;
    private final UserService users;
    private final LateFeePolicy lateFeePolicy;
    private final LateFeePolicy premiumLateFeePolicy = new PremiumLateFeePolicy();
    private final PricingService pricing;
    private final LocationService locations;
    private final ApplicationEventPublisher events;
    private final PromoService promos;
    private final int premiumThreshold;
    private final int holdMinutes;
    private final boolean earlyHandover;

    public BookingService(BookingRepository repository, VehicleService vehicles, UserService users,
                          LateFeePolicy lateFeePolicy, PricingService pricing, LocationService locations,
                          ApplicationEventPublisher events, PromoService promos,
                          @Value("${app.premium.bookings-threshold:10}") int premiumThreshold,
                          @Value("${app.booking.hold-minutes:15}") int holdMinutes,
                          @Value("${app.allow-early-handover:true}") boolean earlyHandover) {
        this.repository = repository;
        this.vehicles = vehicles;
        this.users = users;
        this.lateFeePolicy = lateFeePolicy;
        this.pricing = pricing;
        this.locations = locations;
        this.events = events;
        this.promos = promos;
        this.premiumThreshold = premiumThreshold;
        this.holdMinutes = holdMinutes;
        this.earlyHandover = earlyHandover;
    }

    // ============================================================ availability

    /** Basic check (catalogue search): standard price, self-drive, no locations. */
    public Availability checkAvailability(Long vehicleId, LocalDate pickup, LocalDate returnDate, Long ignoreBookingId) {
        return checkAvailability(vehicleId, pickup, returnDate, ignoreBookingId, CustomerTier.STANDARD, false, null, null);
    }

    public Availability checkAvailability(Long vehicleId, LocalDate pickup, LocalDate returnDate, Long ignoreBookingId,
                                          CustomerTier tier, boolean withDriver, Long pickupLocationId, Long dropLocationId) {
        return checkAvailability(vehicleId, pickup, returnDate, ignoreBookingId, tier, withDriver, pickupLocationId, dropLocationId, null);
    }

    /** Full check for the booking form: availability plus the itemised price for this customer and options. */
    public Availability checkAvailability(Long vehicleId, LocalDate pickup, LocalDate returnDate, Long ignoreBookingId,
                                          CustomerTier tier, boolean withDriver, Long pickupLocationId, Long dropLocationId,
                                          String promoCode) {
        if (pickup == null || returnDate == null) {
            return Availability.no("Choose both a pickup date and a return date");
        }
        LocalDate today = LocalDate.now();
        if (pickup.isBefore(today)) {
            return Availability.no("The pickup date cannot be in the past");
        }
        if (!returnDate.isAfter(pickup)) {
            return Availability.no("The return date must be after the pickup date");
        }
        long days = DateRules.rentalDays(pickup, returnDate);
        if (days > MAX_RENTAL_DAYS) {
            return Availability.no("The longest rental is " + MAX_RENTAL_DAYS + " days");
        }
        Vehicle vehicle = vehicles.findById(vehicleId);
        if (vehicle.isUnderMaintenance()) {
            return Availability.no("This vehicle is currently under maintenance");
        }
        for (Booking other : repository.findByVehicleId(vehicleId)) {
            if (ignoreBookingId != null && ignoreBookingId.equals(other.getId())) {
                continue;
            }
            if (blocksVehicle(other)
                    && DateRules.overlaps(pickup, returnDate, other.getPickupDate(), effectiveEnd(other, today))) {
                return Availability.no("This vehicle is already booked for part of that period. Try different dates.");
            }
        }
        Location from = optionalLocation(pickupLocationId);
        Location to = optionalLocation(dropLocationId);
        PriceQuote quote = pricing.quote(vehicle, tier, pickup, days, withDriver, from, to);
        if (promoCode == null || promoCode.isBlank()) {
            return new Availability(true, "Available for your dates", days, quote.getTotal(), quote.getLines());
        }
        try {
            PromoCode promo = promos.validate(promoCode, tier);
            BigDecimal off = promos.discountFor(promo, rentalPart(quote));
            List<PriceLine> lines = new java.util.ArrayList<>(quote.getLines());
            lines.add(new PriceLine("Promo code " + promo.getCode() + " (" + promo.getPercentOff() + "% off)", off.negate(), "DISCOUNT"));
            return new Availability(true, "Available for your dates. Promo code applied.", days, quote.getTotal().subtract(off), lines);
        } catch (BusinessRuleException ex) {
            return new Availability(true, "Available for your dates. Promo code not applied: " + ex.getMessage() + ".",
                    days, quote.getTotal(), quote.getLines());
        }
    }

    /** The part of the price a promo code applies to: the rental after discounts, without driver and one-way fees. */
    private static BigDecimal rentalPart(PriceQuote quote) {
        return quote.getTotal().subtract(quote.getDriverFee()).subtract(quote.getOneWayFee()).max(BigDecimal.ZERO);
    }

    public int getPremiumThreshold() {
        return premiumThreshold;
    }

    /** Bookings that count towards premium membership: every booking the customer made and did not cancel. */
    public long membershipBookingCount(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED).count();
    }

    private Location optionalLocation(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return locations.findById(id);
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    public PricingService getPricing() {
        return pricing;
    }

    /** Premium members get the gentler late-fee rule. */
    public LateFeePolicy lateFeePolicyFor(CustomerTier tier) {
        return tier == CustomerTier.PREMIUM ? premiumLateFeePolicy : lateFeePolicy;
    }

    /**
     * The nights (dates) on which this vehicle is already taken, from {@code from} (inclusive) to {@code to}
     * (exclusive). The mini calendar on the vehicle page greys these out. Same rules as {@link #checkAvailability}.
     */
    public java.util.List<LocalDate> bookedNights(Long vehicleId, LocalDate from, LocalDate to) {
        java.util.Set<LocalDate> nights = new java.util.TreeSet<>();
        LocalDate today = LocalDate.now();
        for (Booking other : repository.findByVehicleId(vehicleId)) {
            if (!blocksVehicle(other)) {
                continue;
            }
            LocalDate end = effectiveEnd(other, today);
            LocalDate day = other.getPickupDate().isAfter(from) ? other.getPickupDate() : from;
            while (day.isBefore(end) && day.isBefore(to)) {
                nights.add(day);
                day = day.plusDays(1);
            }
        }
        return new java.util.ArrayList<>(nights);
    }

    private boolean blocksVehicle(Booking b) {
        return switch (b.getStatus()) {
            case CONFIRMED, ACTIVE, AWAITING_VERIFICATION -> true;
            case PENDING_PAYMENT -> isHoldActive(b);
            default -> false;
        };
    }

    /** An active rental that is overdue keeps blocking the vehicle until it is actually returned. */
    private LocalDate effectiveEnd(Booking b, LocalDate today) {
        if (b.getStatus() == BookingStatus.ACTIVE && today.isAfter(b.getReturnDate())) {
            return today.plusDays(1);
        }
        return b.getReturnDate();
    }

    private boolean isHoldActive(Booking b) {
        LocalDateTime until = b.getHoldExpiresAt() != null ? b.getHoldExpiresAt() : b.getCreatedAt().plusMinutes(holdMinutes);
        return b.getStatus() == BookingStatus.PENDING_PAYMENT && until.isAfter(LocalDateTime.now());
    }

    public int getHoldMinutes() {
        return holdMinutes;
    }

    public boolean isEarlyHandoverAllowed() {
        return earlyHandover;
    }

    public LateFeePolicy getLateFeePolicy() {
        return lateFeePolicy;
    }

    // ================================================================ Create

    public Booking createBooking(Long customerId, BookingForm form) {
        Customer customer = users.findById(customerId);
        boolean withDriver = form.isWithDriver();
        Vehicle vehicle = vehicles.findById(form.getVehicleId());
        if (withDriver && !vehicle.isDriverAvailable()) {
            throw new BusinessRuleException("A driver cannot be added to a " + vehicle.getType().getLabel().toLowerCase()
                    + " - please choose self-drive");
        }
        if (!withDriver && (form.getLicenceId() == null || form.getLicenceId().isBlank())) {
            throw new BusinessRuleException("Enter your driving licence ID for a self-drive rental, or choose \"With driver\"");
        }
        Location from = locations.requireActive(form.getPickupLocationId(), "pickup");
        Location to = locations.requireActive(form.getDropLocationId(), "drop-off");
        Availability availability = checkAvailability(form.getVehicleId(), form.getPickupDate(), form.getReturnDate(), null,
                customer.getTier(), withDriver, from.getId(), to.getId());
        if (!availability.isAvailable()) {
            throw new BusinessRuleException(availability.getMessage());
        }
        PriceQuote quote = pricing.quote(vehicle, customer.getTier(), form.getPickupDate(), availability.getDays(),
                withDriver, from, to);
        PromoCode promo = null;
        BigDecimal promoOff = BigDecimal.ZERO.setScale(2);
        if (form.getPromoCode() != null) {
            promo = promos.validate(form.getPromoCode(), customer.getTier());    // throws with the reason when it cannot be used
            promoOff = promos.discountFor(promo, rentalPart(quote));
        }

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setVehicleId(vehicle.getId());
        booking.setVehicleModel(vehicle.getFullName());
        booking.setVehicleRegNo(vehicle.getRegistrationNo());
        booking.setPickupDate(form.getPickupDate());
        booking.setPickupTime(form.getPickupTimeValue());
        booking.setPickupLocationId(from.getId());
        booking.setPickupLocationName(from.getLabel());
        booking.setDropLocationId(to.getId());
        booking.setDropLocationName(to.getLabel());
        booking.setPickupAddress(form.getPickupAddress());
        booking.setWithDriver(withDriver);
        booking.setPreferredDriverId(withDriver ? form.getPreferredDriverId() : null);
        booking.setDriverNote(withDriver ? form.getDriverNote() : null);
        booking.setReturnDate(form.getReturnDate());
        applyRenterDetails(booking, form);
        booking.setPricePerDay(vehicle.getPricePerDay());
        booking.setBaseAmount(quote.getBaseAmount());
        booking.setDiscountAmount(quote.getDiscountAmount().add(promoOff));
        booking.setPromoCode(promo == null ? null : promo.getCode());
        booking.setPromoDiscount(promoOff);
        booking.setDriverFee(quote.getDriverFee());
        booking.setOneWayFee(quote.getOneWayFee());
        booking.setPricingNote(promo == null ? quote.getDescription() : quote.getDescription() + " + promo " + promo.getCode());
        booking.setTotalAmount(quote.getTotal().subtract(promoOff));
        booking.setFineAmount(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdMinutes));
        Booking saved = repository.create(booking);

        if (promo != null) {
            promos.recordUse(promo);
        }
        promoteIfEligible(customerId);
        users.saveRentalDetails(customerId, saved.getNic(), saved.getSex(), saved.getAge(),
                saved.getLicenceId() == null || saved.getLicenceId().isBlank() ? customer.getLicenceId() : saved.getLicenceId());
        return saved;
    }

    /** More than {@code premiumThreshold} bookings: the customer becomes a premium member (once). */
    private void promoteIfEligible(Long customerId) {
        Customer customer = users.findById(customerId);
        if (customer.getTier() == CustomerTier.PREMIUM || membershipBookingCount(customerId) <= premiumThreshold) {
            return;
        }
        users.setTier(customerId, CustomerTier.PREMIUM);
        events.publishEvent(new PremiumUnlockedEvent(customerId));
    }

    // ================================================================== Read

    public Booking findById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }

    /** A customer can only ever open their own bookings. */
    public Booking findForCustomer(Long bookingId, Long customerId) {
        Booking booking = findById(bookingId);
        if (!customerId.equals(booking.getCustomerId())) {
            throw ResourceNotFoundException.of("Booking", bookingId);
        }
        return booking;
    }

    public List<Booking> forCustomer(Long customerId) {
        expireStaleHolds();
        return repository.findByCustomerId(customerId).stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed()).toList();
    }

    /** "Active Orders": everything that is not completed or cancelled, soonest pickup first. */
    public List<Booking> activeOrders(Long customerId) {
        return forCustomer(customerId).stream()
                .filter(Booking::isOpen)
                .sorted(Comparator.comparing(Booking::getPickupDate)).toList();
    }

    /** Completed and cancelled bookings, newest first. */
    public List<Booking> history(Long customerId) {
        return forCustomer(customerId).stream().filter(b -> !b.isOpen()).toList();
    }

    public List<Booking> search(String keyword, BookingStatus status) {
        expireStaleHolds();
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findAll().stream()
                .filter(b -> status == null || b.getStatus() == status)
                .filter(b -> k.isEmpty()
                        || b.getReference().toLowerCase().contains(k)
                        || has(b.getRenterName(), k) || has(b.getVehicleModel(), k)
                        || has(b.getVehicleRegNo(), k) || has(b.getNic(), k))
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed()).toList();
    }

    public List<Booking> findByVehicle(Long vehicleId) {
        return repository.findByVehicleId(vehicleId);
    }

    public Map<BookingStatus, Long> countByStatus() {
        expireStaleHolds();
        Map<BookingStatus, Long> counts = new EnumMap<>(BookingStatus.class);
        for (BookingStatus s : BookingStatus.values()) {
            counts.put(s, 0L);
        }
        for (Booking b : repository.findAll()) {
            counts.merge(b.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    public Set<Long> idsWithStatus(BookingStatus status) {
        return repository.findByStatus(status).stream().map(Booking::getId).collect(Collectors.toSet());
    }

    /** The status to show people: derived from live bookings, not just the stored flag. */
    public VehicleStatus displayStatus(Vehicle vehicle) {
        VehicleStatus stored = vehicle.getStatus();
        if (stored != VehicleStatus.AVAILABLE) {
            return stored;
        }
        LocalDate today = LocalDate.now();
        for (Booking b : repository.findByVehicleId(vehicle.getId())) {
            boolean heldNow = (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.AWAITING_VERIFICATION)
                    && !today.isBefore(b.getPickupDate()) && !today.isAfter(b.getReturnDate());
            boolean pendingNow = isHoldActive(b)
                    && !today.isBefore(b.getPickupDate()) && !today.isAfter(b.getReturnDate());
            if (heldNow || pendingNow) {
                return VehicleStatus.RESERVED;
            }
        }
        return VehicleStatus.AVAILABLE;
    }

    /** True while a vehicle still has bookings that must be honoured (blocks deleting the vehicle). */
    public boolean hasOpenBookings(Long vehicleId) {
        return repository.findByVehicleId(vehicleId).stream().anyMatch(b ->
                b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.ACTIVE
                        || b.getStatus() == BookingStatus.AWAITING_VERIFICATION
                        || b.getStatus() == BookingStatus.FINE_DUE || isHoldActive(b));
    }

    public List<VehicleRentalCount> mostRented(int limit) {
        Map<String, Long> counts = new HashMap<>();
        for (Booking b : repository.findAll()) {
            if (b.getStatus() == BookingStatus.ACTIVE || b.getStatus() == BookingStatus.FINE_DUE
                    || b.getStatus() == BookingStatus.COMPLETED) {
                counts.merge(b.getVehicleModel(), 1L, Long::sum);
            }
        }
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(1);
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new VehicleRentalCount(e.getKey(), e.getValue(), (int) Math.round(e.getValue() * 100.0 / max)))
                .toList();
    }

    // ================================================================ Update

    /** Admin correction of the renter's details on an open booking. */
    public Booking updateRenterDetails(Long bookingId, BookingForm form) {
        Booking booking = findById(bookingId);
        if (!booking.isOpen()) {
            throw new BusinessRuleException("Completed or cancelled bookings can no longer be edited");
        }
        applyRenterDetails(booking, form);
        return repository.update(booking);
    }

    /** Payment received for the rental: PENDING_PAYMENT -> CONFIRMED. Called by PaymentService. */
    public Booking markPaid(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("This booking is not waiting for payment");
        }
        if (!isHoldActive(booking)) {
            booking.setStatus(BookingStatus.CANCELLED);
            repository.update(booking);
            throw new BusinessRuleException("The " + holdMinutes + "-minute payment window has expired. "
                    + "Please start a new booking.");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = repository.update(booking);
        events.publishEvent(new BookingConfirmedEvent(saved.getId()));
        return saved;
    }

    /** A bank transfer was submitted: the vehicle stays blocked until the admin checks the money arrived. */
    public Booking markAwaitingVerification(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessRuleException("This booking is not waiting for payment");
        }
        if (!isHoldActive(booking)) {
            booking.setStatus(BookingStatus.CANCELLED);
            repository.update(booking);
            throw new BusinessRuleException("The " + holdMinutes + "-minute payment window has expired. "
                    + "Please start a new booking.");
        }
        booking.setStatus(BookingStatus.AWAITING_VERIFICATION);
        return repository.update(booking);
    }

    /** Admin approved the transfer: AWAITING_VERIFICATION -> CONFIRMED. */
    public Booking confirmVerified(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.AWAITING_VERIFICATION) {
            throw new BusinessRuleException("This booking is not waiting for payment verification");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = repository.update(booking);
        events.publishEvent(new BookingConfirmedEvent(saved.getId()));
        return saved;
    }

    /** Admin rejected the transfer: the customer gets a fresh payment window to try another method. */
    public Booking rejectVerification(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.AWAITING_VERIFICATION) {
            throw new BusinessRuleException("This booking is not waiting for payment verification");
        }
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdMinutes));
        return repository.update(booking);
    }

    /** Called by the driver module when a driver is (re)assigned. Pass null to clear. */
    public Booking assignDriver(Long bookingId, Long driverId, String driverName, String driverPhone) {
        Booking booking = findById(bookingId);
        booking.setDriverId(driverId);
        booking.setDriverName(driverName);
        booking.setDriverPhone(driverPhone);
        return repository.update(booking);
    }

    public Booking cancelByCustomer(Long bookingId, Long customerId) {
        Booking booking = findForCustomer(bookingId, customerId);
        if (!booking.isCancellableByCustomer(LocalDate.now())) {
            throw new BusinessRuleException("This booking can no longer be cancelled online. "
                    + "Please contact the rental company.");
        }
        return cancel(booking);
    }

    public Booking cancelByAdmin(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.FINE_DUE) {
            throw new BusinessRuleException("A " + booking.getStatus().getLabel().toLowerCase()
                    + " booking cannot be cancelled");
        }
        return cancel(booking);
    }

    private Booking cancel(Booking booking) {
        boolean wasActive = booking.getStatus() == BookingStatus.ACTIVE;
        booking.setStatus(BookingStatus.CANCELLED);
        repository.update(booking);
        if (wasActive) {
            releaseVehicle(booking.getVehicleId(), VehicleStatus.AVAILABLE);
        }
        events.publishEvent(new BookingCancelledEvent(booking.getId()));
        return booking;
    }

    /** Pickup: the admin has checked the original NIC and licence, booking becomes ACTIVE. */
    public Booking activate(Long bookingId, boolean documentsVerified, String adminName) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Only a confirmed (paid) booking can be handed over");
        }
        if (!documentsVerified) {
            throw new BusinessRuleException("Tick the box to confirm the original NIC and driving licence were verified");
        }
        if (!earlyHandover && LocalDate.now().isBefore(booking.getPickupDate())) {
            throw new BusinessRuleException("The rental starts on " + booking.getPickupDate()
                    + ". The vehicle cannot be handed over earlier.");
        }
        Vehicle vehicle = vehicles.findById(booking.getVehicleId());
        if (vehicle.getStatus() == VehicleStatus.RENTED || vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            throw new BusinessRuleException("The vehicle is currently " + vehicle.getStatus().getLabel().toLowerCase()
                    + " and cannot be handed over");
        }
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setDocsVerified(true);
        booking.setVerifiedBy(adminName);
        repository.update(booking);
        vehicles.updateStatus(vehicle.getId(), VehicleStatus.RENTED);
        return booking;
    }

    /**
     * Return: the admin enters the real return date and inspects the vehicle.
     * Late days x 10% of the ORIGINAL amount is added as a fine. No fine -> COMPLETED, otherwise FINE_DUE.
     */
    public Booking processReturn(Long bookingId, ReturnForm form) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new BusinessRuleException("Only an active rental can be returned");
        }
        LocalDate actual = form.getActualReturnDate();
        if (actual.isBefore(booking.getPickupDate())) {
            throw new BusinessRuleException("The return date cannot be before the pickup date");
        }
        if (!earlyHandover && actual.isAfter(LocalDate.now())) {
            throw new BusinessRuleException("The return date cannot be in the future");
        }
        long lateDays = DateRules.lateDays(booking.getReturnDate(), actual);
        CustomerTier tier = users.findById(booking.getCustomerId()).getTier();
        BigDecimal fine = lateFeePolicyFor(tier).calculateFine(booking.getTotalAmount(), lateDays);

        booking.setActualReturnDate(actual);
        booking.setLateDays(lateDays);
        booking.setFineAmount(fine);
        booking.setConditionNote(form.getConditionNote());
        booking.setStatus(fine.signum() > 0 ? BookingStatus.FINE_DUE : BookingStatus.COMPLETED);
        repository.update(booking);

        releaseVehicle(booking.getVehicleId(),
                form.isNeedsMaintenance() ? VehicleStatus.MAINTENANCE : VehicleStatus.AVAILABLE);
        return booking;
    }

    /** The late fine has been paid: FINE_DUE -> COMPLETED. Called by PaymentService. */
    public Booking completeAfterFine(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.FINE_DUE) {
            throw new BusinessRuleException("This booking has no late fine to pay");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return repository.update(booking);
    }

    // ================================================================ Delete

    /** Admin housekeeping: only finished (completed / cancelled) bookings can be deleted. */
    public void delete(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.isOpen()) {
            throw new BusinessRuleException("Only completed or cancelled bookings can be deleted");
        }
        repository.deleteById(bookingId);
    }

    // ============================================================== internals

    private void applyRenterDetails(Booking booking, BookingForm form) {
        booking.setRenterName(form.getRenterName());
        booking.setNic(form.getNic());
        booking.setSex(form.getSex());
        booking.setAge(form.getAge());
        booking.setLicenceId(form.getLicenceId());
    }

    private void releaseVehicle(Long vehicleId, VehicleStatus status) {
        try {
            vehicles.updateStatus(vehicleId, status);
        } catch (ResourceNotFoundException ignored) {
            // the vehicle was removed from the fleet meanwhile - nothing to update
        }
    }

    /** Unpaid bookings whose hold ran out are cancelled so they stop cluttering the lists. */
    private void expireStaleHolds() {
        for (Booking b : repository.findByStatus(BookingStatus.PENDING_PAYMENT)) {
            if (!isHoldActive(b)) {
                b.setStatus(BookingStatus.CANCELLED);
                repository.update(b);
            }
        }
    }

    private static boolean has(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }
}
