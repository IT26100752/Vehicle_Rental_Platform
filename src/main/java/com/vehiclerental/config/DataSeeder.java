package com.vehiclerental.config;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.booking.repository.BookingRepository;
import com.vehiclerental.common.util.PasswordUtil;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.repository.PaymentRepository;
import com.vehiclerental.review.entity.Review;
import com.vehiclerental.review.entity.ReviewStatus;
import com.vehiclerental.review.repository.ReviewRepository;
import com.vehiclerental.user.entity.CustomerUser;
import com.vehiclerental.user.repository.UserRepository;
import com.vehiclerental.vehicle.entity.Bike;
import com.vehiclerental.vehicle.entity.Car;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.Transmission;
import com.vehiclerental.vehicle.entity.Truck;
import com.vehiclerental.vehicle.entity.Van;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.repository.VehicleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds the storage (MySQL tables OR JSON files, whichever mode is active)
 * with SAMPLE DATA the first time the app runs on an empty storage.
 * The deliverable list asks for sample data for the demonstration.
 *
 * Demo logins created here:
 *   admin   / admin123      (AdminUser, permission level 3)
 *   nimal   / nimal123      (premium customer)
 *   kasun   / kasun123      (regular customer)
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final VehicleRepository vehicles;
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final ReviewRepository reviews;

    public DataSeeder(UserRepository users, VehicleRepository vehicles,
                      BookingRepository bookings, PaymentRepository payments,
                      ReviewRepository reviews) {
        this.users = users;
        this.vehicles = vehicles;
        this.bookings = bookings;
        this.payments = payments;
        this.reviews = reviews;
    }

    @Override
    public void run(ApplicationArguments args) {
        refreshFleetPhotos();
        if (!users.isEmpty()) {
            log.info("Sample data already present - skipping seed.");
            return;
        }
        log.info("Seeding sample data ...");

        /* --------------------------- users --------------------------- */
        users.save(new AdminUser(0, "Deshan Perera", "admin",
                PasswordUtil.hash("admin123"), "admin@vrs.lk", "0771000000",
                "Operations", 3));
        users.save(new CustomerUser(0, "Nimal Silva", "nimal",
                PasswordUtil.hash("nimal123"), "nimal@gmail.com", "0771234567",
                "940001234V", "Colombo", true));
        users.save(new CustomerUser(0, "Kasun Fernando", "kasun",
                PasswordUtil.hash("kasun123"), "kasun@gmail.com", "0712223344",
                "980005678V", "Kandy", false));

        /* ------------------------- vehicles -------------------------- */
        vehicles.save(new Car(0, "CBA-1234", "Toyota", "Axio", 2019, 8500,
                FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleStatus.AVAILABLE,
                "/images/vehicles/axio.jpg", 4, 450));
        vehicles.save(new Van(0, "CBA-5678", "Toyota", "KDH Super GL", 2020, 15000,
                FuelType.DIESEL, Transmission.MANUAL, 14, VehicleStatus.RENTED,
                "/images/vehicles/kdh.jpg", 900, true));
        vehicles.save(new Bike(0, "WP-BB-0123", "Yamaha", "FZ V3", 2022, 3500,
                FuelType.PETROL, Transmission.MANUAL, 2, VehicleStatus.AVAILABLE,
                "/images/vehicles/fz.jpg", 149, true));
        vehicles.save(new Truck(0, "CBA-9012", "Mitsubishi", "Canter", 2018, 18000,
                FuelType.DIESEL, Transmission.MANUAL, 3, VehicleStatus.AVAILABLE,
                "/images/vehicles/canter.jpg", 3500, "box"));
        vehicles.save(new Car(0, "CBA-3456", "Nissan", "Leaf", 2021, 9500,
                FuelType.ELECTRIC, Transmission.AUTOMATIC, 5, VehicleStatus.AVAILABLE,
                "/images/vehicles/leaf.jpg", 5, 435));
        vehicles.save(new Van(0, "CBA-7890", "Nissan", "Caravan", 2017, 12000,
                FuelType.DIESEL, Transmission.MANUAL, 12, VehicleStatus.IN_MAINTENANCE,
                "/images/vehicles/caravan.jpg", 700, false));

        /* ------------------------- bookings -------------------------- */
        LocalDate today = LocalDate.now();
        Booking done = bookings.save(new Booking(0, 2, 1,
                today.minusDays(20), today.minusDays(15),
                "Colombo", "Colombo", 5 * 8500.0, BookingStatus.COMPLETED));
        done.setReturnedDate(today.minusDays(15));
        done.setFine(0);
        bookings.update(done);

        bookings.save(new Booking(0, 3, 2,
                today.minusDays(3), today.plusDays(4),
                "Kandy", "Colombo", 7 * 15000.0, BookingStatus.ACTIVE));

        /* ------------------------- payments -------------------------- */
        payments.save(new Payment(0, done.getId(), done.getTotalAmount(),
                PaymentMethod.CASH, today.minusDays(15), PaymentStatus.COMPLETED,
                "TXN-SEED0001"));
        payments.save(new Payment(0, 2, 7 * 15000.0,
                PaymentMethod.ONLINE, today.minusDays(3), PaymentStatus.PENDING,
                "TXN-SEED0002"));

        /* ------------------------- reviews --------------------------- */
        reviews.save(new Review(0, 2, 1, 5,
                "Clean car, smooth pickup in Colombo. Highly recommended!",
                today.minusDays(14), ReviewStatus.APPROVED));
        reviews.save(new Review(0, 3, 2, 4,
                "Spacious van, driver seat a bit high but overall great.",
                today.minusDays(2), ReviewStatus.APPROVED));
        reviews.save(new Review(0, 3, 3, 5,
                "Best bike rental in town, helmet was included.",
                today.minusDays(1), ReviewStatus.PENDING));

        log.info("Sample data seeded.");
    }

    /**
     * The sample fleet ships with real JPEG photos since build 2026-10-10e.
     * Older stores (existing MySQL rows or JSON files) may still point at the
     * retired SVG placeholders, so re-point them on every start-up.
     * Vehicles added by users are never touched.
     */
    private void refreshFleetPhotos() {
        java.util.Map<String, String> photos = java.util.Map.of(
                "CBA-1234", "/images/vehicles/axio.jpg",
                "CBA-5678", "/images/vehicles/kdh.jpg",
                "WP-BB-0123", "/images/vehicles/fz.jpg",
                "CBA-9012", "/images/vehicles/canter.jpg",
                "CBA-3456", "/images/vehicles/leaf.jpg",
                "CBA-7890", "/images/vehicles/caravan.jpg");
        for (Vehicle v : vehicles.findAll()) {
            String photo = photos.get(v.getVehicleNumber());
            if (photo == null || photo.equals(v.getImageUrl())) {
                continue;
            }
            String cur = v.getImageUrl();
            if (cur == null || cur.isEmpty() || cur.endsWith(".svg")) {
                v.setImageUrl(photo);
                vehicles.update(v);
            }
        }
    }
}
