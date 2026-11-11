package com.vehiclerental.config;

import com.vehiclerental.admin.entity.Admin;
import com.vehiclerental.admin.entity.AdminLevel;
import com.vehiclerental.admin.repository.AdminRepository;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.pricing.PriceQuote;
import com.vehiclerental.booking.pricing.PricingService;
import com.vehiclerental.booking.repository.BookingRepository;
import com.vehiclerental.common.DateRules;
import com.vehiclerental.complaint.dto.ComplaintForm;
import com.vehiclerental.complaint.entity.ComplaintCategory;
import com.vehiclerental.complaint.service.ComplaintService;
import com.vehiclerental.driver.entity.AssignmentStatus;
import com.vehiclerental.driver.entity.Driver;
import com.vehiclerental.driver.entity.DriverAssignment;
import com.vehiclerental.driver.entity.DriverStatus;
import com.vehiclerental.driver.repository.DriverAssignmentRepository;
import com.vehiclerental.driver.repository.DriverRepository;
import com.vehiclerental.location.entity.Location;
import com.vehiclerental.location.repository.LocationRepository;
import com.vehiclerental.notification.NotificationService;
import com.vehiclerental.notification.NotificationType;
import com.vehiclerental.payment.entity.BankTransferPayment;
import com.vehiclerental.payment.entity.CardPayment;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.repository.PaymentRepository;
import com.vehiclerental.payment.service.ReceiptService;
import com.vehiclerental.review.entity.Review;
import com.vehiclerental.review.repository.ReviewRepository;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.CustomerTier;
import com.vehiclerental.user.repository.UserRepository;
import com.vehiclerental.vehicle.entity.*;
import com.vehiclerental.vehicle.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * First start only: every empty data file is filled with demo data so the whole system can be shown straight away.
 *   Admin            : admin / Admin@123
 *   Customer         : kasun@rental.lk / Demo@123         (standard)
 *   Premium customer : premium@rental.lk / Premium@123   (10% off 3+ day rentals, softer late fee)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository admins;
    private final UserRepository users;
    private final VehicleRepository vehicles;
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final ReviewRepository reviews;
    private final LocationRepository locations;
    private final DriverRepository drivers;
    private final DriverAssignmentRepository assignments;
    private final PricingService pricing;
    private final ComplaintService complaints;
    private final NotificationService notifications;
    private final PasswordEncoder encoder;
    private final com.vehiclerental.promo.repository.PromoRepository promoCodes;

    public DataSeeder(AdminRepository admins, UserRepository users, VehicleRepository vehicles,
                      BookingRepository bookings, PaymentRepository payments, ReviewRepository reviews,
                      LocationRepository locations, DriverRepository drivers, DriverAssignmentRepository assignments,
                      PricingService pricing, ComplaintService complaints, NotificationService notifications,
                      PasswordEncoder encoder, com.vehiclerental.promo.repository.PromoRepository promoCodes) {
        this.promoCodes = promoCodes;
        this.admins = admins;
        this.users = users;
        this.vehicles = vehicles;
        this.bookings = bookings;
        this.payments = payments;
        this.reviews = reviews;
        this.locations = locations;
        this.drivers = drivers;
        this.assignments = assignments;
        this.pricing = pricing;
        this.complaints = complaints;
        this.notifications = notifications;
        this.encoder = encoder;
    }

    private void seedPromo(String code, String description, int percent, int maxDiscount, boolean premiumOnly) {
        if (promoCodes.findByCode(code).isPresent()) {
            return;
        }
        com.vehiclerental.promo.entity.PromoCode p = new com.vehiclerental.promo.entity.PromoCode();
        p.setCode(code);
        p.setDescription(description);
        p.setPercentOff(percent);
        p.setMaxDiscount(java.math.BigDecimal.valueOf(maxDiscount));
        p.setPremiumOnly(premiumOnly);
        p.setValidFrom(java.time.LocalDate.now());
        p.setValidTo(java.time.LocalDate.now().plusMonths(6));
        promoCodes.create(p);
    }

    private void seedAdmin(String username, String fullName, String email, String password, AdminLevel level) {
        if (admins.findByUsername(username).isPresent()) {
            return;
        }
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setPhone("0112345678");
        admin.setLevel(level);
        admin.setPasswordHash(encoder.encode(password));
        admins.create(admin);
    }

    @Override
    public void run(String... args) {
        // One manager admin (can add / delete admins) and four ordinary admins. Each is created only if missing,
        // so existing data is never overwritten.
        seedAdmin("manageradmin", "Manager Admin", "manager@vehiclerental.lk", "ManagerAdmin@123", AdminLevel.MANAGER);
        seedAdmin("admin1", "Admin One", "admin1@vehiclerental.lk", "Admin1@123", AdminLevel.ADMIN);
        seedAdmin("admin2", "Admin Two", "admin2@vehiclerental.lk", "Admin2@123", AdminLevel.ADMIN);
        seedAdmin("admin3", "Admin Three", "admin3@vehiclerental.lk", "Admin3@123", AdminLevel.ADMIN);
        seedAdmin("admin4", "Admin Four", "admin4@vehiclerental.lk", "Admin4@123", AdminLevel.ADMIN);
        seedPromo("WELCOME10", "10% off your rental", 10, 5000, false);
        seedPromo("PREMIUM15", "Extra 15% off for premium members", 15, 10000, true);
        Customer demo = users.findByEmail("kasun@rental.lk").orElse(null);
        Customer premium = users.findByEmail("premium@rental.lk").orElse(null);
        if (users.count() == 0) {
            demo = customer("Kasun Perera", "kasun@rental.lk", "0771234567", "Colombo 07", "Demo@123", CustomerTier.STANDARD);
            premium = customer("Nimali Perera", "premium@rental.lk", "0712345678", "Kandy", "Premium@123", CustomerTier.PREMIUM);
        }
        if (locations.count() == 0) {
            seedLocations();
        }
        if (drivers.count() == 0) {
            seedDrivers();
        }
        if (vehicles.count() == 0) {
            seedVehicles();
        }
        if (bookings.count() == 0 && demo != null && premium != null) {
            seedBookings(demo, premium);
        }
    }

    // ============================================================== reference data

    private Customer customer(String name, String email, String phone, String address, String password, CustomerTier tier) {
        Customer c = new Customer();
        c.setFullName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        c.setPasswordHash(encoder.encode(password));
        c.setTier(tier);
        c.setEmailVerified(true);          // the seeded accounts can log in straight away
        return users.create(c);
    }

    private void seedLocations() {
        location("Colombo Fort", "Colombo", "12 Olcott Mawatha, Colombo 01", "0112000001", 6.9344, 79.8428);
        location("Bandaranaike Airport", "Katunayake", "Arrivals hall, BIA", "0112000002", 7.1808, 79.8841);
        location("Kandy City", "Kandy", "88 Peradeniya Road, Kandy", "0812000003", 7.2906, 80.6337);
        location("Galle Fort", "Galle", "5 Church Street, Galle", "0912000004", 6.0267, 80.2170);
        location("Negombo Beach", "Negombo", "30 Lewis Place, Negombo", "0312000005", 7.2083, 79.8358);
    }

    private void location(String name, String city, String address, String phone, double lat, double lng) {
        Location l = new Location();
        l.setName(name);
        l.setCity(city);
        l.setAddress(address);
        l.setPhone(phone);
        l.setActive(true);
        l.setLatitude(lat);
        l.setLongitude(lng);
        locations.create(l);
    }

    private void seedDrivers() {
        driver("Kasun Fernando", "0771110001", "amal.driver41@gmail.com", "B2345671", "Sinhala, English", DriverStatus.AVAILABLE);
        driver("Ravindran Kumar", "0771110002", "ravi.driver@rental.lk", "B2345672", "Tamil, Sinhala, English", DriverStatus.AVAILABLE);
        driver("Sunil Jayasuriya", "0771110003", "sunil.driver@rental.lk", "B2345673", "Sinhala", DriverStatus.OFF_DUTY);
    }

    private void driver(String name, String phone, String email, String licence, String languages, DriverStatus status) {
        Driver d = new Driver();
        d.setFullName(name);
        d.setPhone(phone);
        d.setEmail(email);
        d.setLicenceNo(licence);
        d.setLanguages(languages);
        d.setStatus(status);
        drivers.create(d);
    }

    // ================================================================= the fleet

    private void seedVehicles() {
        Car axio = (Car) vehicle(VehicleType.CAR, "Toyota", "Axio", 2018, Transmission.AUTOMATIC, "CAD-7783", "8800",
                FuelType.PETROL, 5, "/images/vehicles/axio.jpg", "Reliable family sedan with a large boot.");
        axio.setDoors(4);
        axio.setBootLitres(450);
        vehicles.update(axio);
        Car leaf = (Car) vehicle(VehicleType.CAR, "Nissan", "Leaf", 2019, Transmission.AUTOMATIC, "EV-1180", "11000",
                FuelType.ELECTRIC, 5, "/images/vehicles/leaf.jpg", "Quiet, zero-emission electric car.");
        leaf.setBootLitres(370);
        vehicles.update(leaf);
        vehicle(VehicleType.CAR, "Honda", "City", 2020, Transmission.AUTOMATIC, "CBK-3345", "10500",
                FuelType.PETROL, 5, "/images/photos/car.jpg", "Smooth silver sedan, ideal for business trips.");

        Van kdh = (Van) vehicle(VehicleType.VAN, "Toyota", "KDH", 2017, Transmission.AUTOMATIC, "NC-4410", "21000",
                FuelType.DIESEL, 10, "/images/vehicles/kdh.jpg", "Comfortable 10-seater, popular for family trips.");
        kdh.setCargoKg(600);
        vehicles.update(kdh);
        Van caravan = (Van) vehicle(VehicleType.VAN, "Nissan", "Caravan", 2016, Transmission.MANUAL, "NB-1377", "18500",
                FuelType.DIESEL, 9, "/images/vehicles/caravan.jpg", "Roomy 9-seater with lots of luggage space.");
        caravan.setCargoKg(800);
        vehicles.update(caravan);
        vehicle(VehicleType.VAN, "Ford", "Transit", 2019, Transmission.MANUAL, "NB-8821", "23000",
                FuelType.DIESEL, 14, "/images/photos/van.jpg", "14-seat tourist van with powerful A/C.");

        Bike fz = (Bike) vehicle(VehicleType.BIKE, "Yamaha", "FZ V3", 2021, Transmission.MANUAL, "BKA-2204", "3500",
                FuelType.PETROL, 2, "/images/vehicles/fz.jpg", "Light, nimble and thrifty. Helmet included.");
        fz.setEngineCc(150);
        vehicles.update(fz);
        Vehicle sport = vehicle(VehicleType.BIKE, "Ducati", "Monster", 2020, Transmission.MANUAL, "BKB-9001", "9500",
                FuelType.PETROL, 2, "/images/photos/bike.jpg", "Powerful 800cc sport bike for experienced riders.");
        ((Bike) sport).setEngineCc(800);
        sport.setStatus(VehicleStatus.MAINTENANCE);
        vehicles.update(sport);

        Truck canter = (Truck) vehicle(VehicleType.TRUCK, "Mitsubishi", "Canter", 2018, Transmission.MANUAL, "LD-5530", "16000",
                FuelType.DIESEL, 3, "/images/vehicles/canter.jpg", "Reliable light truck for moving house or deliveries.");
        canter.setMaxLoadKg(3000);
        canter.setBodyKind("Flatbed");
        vehicles.update(canter);
        Truck isuzu = (Truck) vehicle(VehicleType.TRUCK, "Isuzu", "NPR", 2017, Transmission.MANUAL, "LD-6618", "19500",
                FuelType.DIESEL, 3, "/images/photos/truck.jpg", "Box truck with a roomy, weather-proof body.");
        isuzu.setMaxLoadKg(4000);
        isuzu.setBodyKind("Box");
        vehicles.update(isuzu);
    }

    private Vehicle vehicle(VehicleType type, String brand, String model, int year, Transmission gearbox, String reg,
                            String price, FuelType fuel, int seats, String image, String description) {
        Vehicle v = VehicleRepository.create(type);
        v.setBrand(brand);
        v.setModel(model);
        v.setYear(year);
        v.setTransmission(gearbox);
        v.setRegistrationNo(reg);
        v.setPricePerDay(new BigDecimal(price));
        v.setFuelType(fuel);
        v.setSeatingCapacity(seats);
        v.setImageUrl(image);
        v.setDescription(description);
        return vehicles.create(v);
    }

    // ================================================================== bookings

    private void seedBookings(Customer demo, Customer premium) {
        LocalDate today = LocalDate.now();
        Location fort = locations.findAll().get(0);
        Location airport = locations.findAll().get(1);
        Location kandy = locations.findAll().get(2);
        Location galle = locations.findAll().get(3);
        Vehicle axio = vehicles.findByRegistrationNo("CAD-7783").orElseThrow();
        Vehicle leaf = vehicles.findByRegistrationNo("EV-1180").orElseThrow();
        Vehicle kdh = vehicles.findByRegistrationNo("NC-4410").orElseThrow();
        Vehicle caravan = vehicles.findByRegistrationNo("NB-1377").orElseThrow();
        Vehicle fz = vehicles.findByRegistrationNo("BKA-2204").orElseThrow();
        Vehicle canter = vehicles.findByRegistrationNo("LD-5530").orElseThrow();

        // 1) A finished self-drive rental with a 5 star review
        Booking done = booking(demo, axio, today.minusDays(20), today.minusDays(17), BookingStatus.COMPLETED, false, fort, fort);
        done.setActualReturnDate(today.minusDays(17));
        done.setDocsVerified(true);
        done.setVerifiedBy("admin");
        done.setConditionNote("Returned clean, no damage.");
        bookings.update(done);
        pay(done, PaymentKind.RENTAL);
        review(demo, done, axio, 5, "Spotless car and very smooth pickup. Great service!");

        // 2) A finished premium rental with a LOW review: shows that bad reviews are accepted and flagged to the admin
        Booking truckDone = booking(premium, canter, today.minusDays(12), today.minusDays(9), BookingStatus.COMPLETED, false, kandy, kandy);
        truckDone.setActualReturnDate(today.minusDays(9));
        truckDone.setDocsVerified(true);
        truckDone.setVerifiedBy("admin");
        bookings.update(truckDone);
        pay(truckDone, PaymentKind.RENTAL);
        review(premium, truckDone, canter, 2, "The truck was not as clean as I expected and pickup was 40 minutes late.");

        // 3) An ACTIVE rental that is already overdue: return it on the admin page to see the late fine
        Booking overdue = booking(demo, leaf, today.minusDays(5), today.minusDays(2), BookingStatus.ACTIVE, false, airport, airport);
        overdue.setDocsVerified(true);
        overdue.setVerifiedBy("admin");
        bookings.update(overdue);
        pay(overdue, PaymentKind.RENTAL);
        leaf.setStatus(VehicleStatus.RENTED);
        vehicles.update(leaf);

        // 4) A paid booking WITH A DRIVER, one-way (Colombo to Kandy), driver already assigned and notified
        Booking trip = booking(premium, kdh, today.plusDays(3), today.plusDays(6), BookingStatus.CONFIRMED, true, fort, kandy);
        Driver kasun = drivers.findAll().get(0);
        trip.setDriverId(kasun.getId());
        trip.setDriverName(kasun.getFullName());
        trip.setDriverPhone(kasun.getPhone());
        bookings.update(trip);
        pay(trip, PaymentKind.RENTAL);
        DriverAssignment a = new DriverAssignment();
        a.setBookingId(trip.getId());
        a.setDriverId(kasun.getId());
        a.setPickupDate(trip.getPickupDate());
        a.setReturnDate(trip.getReturnDate());
        a.setStatus(AssignmentStatus.ASSIGNED);
        a.setToken("demo" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 28));
        a.setNotifiedAt(LocalDateTime.now());
        assignments.create(a);

        // 5) A with-driver booking paid by bank transfer: waits for the admin to verify the money
        Booking transfer = booking(demo, caravan, today.plusDays(8), today.plusDays(10), BookingStatus.AWAITING_VERIFICATION, true, galle, galle);
        BankTransferPayment slip = new BankTransferPayment();
        slip.setBookingId(transfer.getId());
        slip.setCustomerId(demo.getId());
        slip.setAmount(transfer.getTotalAmount());
        slip.setKind(PaymentKind.RENTAL);
        slip.setStatus(PaymentStatus.PENDING_VERIFICATION);
        slip.setReference("PAY-SEED" + String.format("%04d", transfer.getId()));
        slip.setBankName("Sampath Bank");
        slip.setSlipReference("TXN884201");
        slip.setTransferDate(today);
        slip.setCreatedAt(LocalDateTime.now().minusHours(2));
        payments.create(slip);

        // 6) A cancelled booking (refunded)
        Booking cancelled = booking(demo, fz, today.plusDays(10), today.plusDays(12), BookingStatus.CANCELLED, false, fort, fort);
        Payment refunded = pay(cancelled, PaymentKind.RENTAL);
        refunded.setStatus(PaymentStatus.REFUNDED);
        payments.update(refunded);

        // a complaint and the matching bell notifications so the admin side is not empty on first start
        ComplaintForm complaint = new ComplaintForm();
        complaint.setBookingId(truckDone.getId());
        complaint.setCategory(ComplaintCategory.STAFF_SERVICE);
        complaint.setSubject("Late handover at Kandy");
        complaint.setDescription("The truck was handed over 40 minutes late and the cabin had not been cleaned.");
        complaints.file(premium.getId(), complaint);
        notifications.notifyAdmins(NotificationType.PAYMENT, "Bank transfer to verify (" + transfer.getReference() + ")",
                demo.getFullName() + " says they transferred LKR " + String.format("%,.2f", transfer.getTotalAmount())
                        + " (Sampath Bank). Please check the bank account and approve or reject it.",
                "/admin/payments?status=PENDING_VERIFICATION");
    }

    // ------------------------------------------------------------- helpers

    private Booking booking(Customer c, Vehicle v, LocalDate pickup, LocalDate ret, BookingStatus status,
                            boolean withDriver, Location from, Location to) {
        long days = DateRules.rentalDays(pickup, ret);
        PriceQuote quote = pricing.quote(v, c.getTier(), pickup, days, withDriver && v.isDriverAvailable(), from, to);
        Booking b = new Booking();
        b.setCustomerId(c.getId());
        b.setVehicleId(v.getId());
        b.setVehicleModel(v.getFullName());
        b.setVehicleRegNo(v.getRegistrationNo());
        b.setPickupDate(pickup);
        b.setReturnDate(ret);
        b.setPickupTime(LocalTime.of(9, 0));
        b.setPickupLocationId(from.getId());
        b.setPickupLocationName(from.getLabel());
        b.setDropLocationId(to.getId());
        b.setDropLocationName(to.getLabel());
        b.setWithDriver(withDriver && v.isDriverAvailable());
        b.setRenterName(c.getFullName());
        b.setNic("200012345678");
        b.setSex("Male");
        b.setAge(25);
        b.setLicenceId(b.isWithDriver() ? "" : "B1234567");
        b.setPricePerDay(v.getPricePerDay());
        b.setBaseAmount(quote.getBaseAmount());
        b.setDiscountAmount(quote.getDiscountAmount());
        b.setDriverFee(quote.getDriverFee());
        b.setOneWayFee(quote.getOneWayFee());
        b.setPricingNote(quote.getDescription());
        b.setTotalAmount(quote.getTotal());
        b.setFineAmount(BigDecimal.ZERO);
        b.setStatus(status);
        // booked two days before pickup, but never in the future
        LocalDateTime created = pickup.minusDays(2).atTime(10, 0);
        b.setCreatedAt(created.isAfter(LocalDateTime.now()) ? LocalDateTime.now().minusDays(1) : created);
        return bookings.create(b);
    }

    private Payment pay(Booking b, PaymentKind kind) {
        CardPayment p = new CardPayment();
        p.setBookingId(b.getId());
        p.setCustomerId(b.getCustomerId());
        p.setAmount(b.getTotalAmount());
        p.setKind(kind);
        p.setStatus(PaymentStatus.PAID);
        p.setReference("PAY-SEED" + String.format("%04d", b.getId()));
        p.setCardHolder(b.getRenterName());
        p.setCardLast4("4242");
        p.setCardBrand("Visa");
        p.setCreatedAt(b.getCreatedAt().plusMinutes(5));
        p.setPaidAt(p.getCreatedAt());
        Payment saved = payments.create(p);
        saved.setPaidAt(saved.getCreatedAt());
        saved.setReceiptNo(ReceiptService.receiptNumberFor(saved));
        return payments.update(saved);
    }

    private void review(Customer c, Booking b, Vehicle v, int rating, String comment) {
        Review r = new Review();
        r.setBookingId(b.getId());
        r.setCustomerId(c.getId());
        r.setCustomerName(c.getFullName());
        r.setVehicleId(v.getId());
        r.setVehicleModel(v.getFullName());
        r.setRating(rating);
        r.setComment(comment);
        reviews.create(r);
    }
}
