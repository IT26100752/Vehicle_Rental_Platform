package com.vehiclerental.config;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.repository.BookingMapping;
import com.vehiclerental.common.repository.DataStore;
import com.vehiclerental.common.repository.JsonFileStore;
import com.vehiclerental.common.repository.MySqlStore;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.repository.PaymentMapping;
import com.vehiclerental.review.entity.Review;
import com.vehiclerental.review.repository.ReviewMapping;
import com.vehiclerental.user.entity.User;
import com.vehiclerental.user.repository.UserMapping;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.repository.VehicleMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;

/**
 * THE STORAGE SWITCH (Strategy pattern, chosen at start-up)
 * ---------------------------------------------------------
 *   app.storage = mysql   -> every entity lives in a MySQL table
 *   app.storage = file    -> every entity lives in a JSON text file
 *
 * Nothing else in the application changes: repositories, services and
 * controllers all program against the DataStore interface.
 */
@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    private final boolean useMySql;
    private final String dataDir;
    private final JdbcTemplate jdbc;

    public StorageConfig(@Value("${app.storage:file}") String storage,
                         @Value("${app.data-dir}") String dataDir,
                         JdbcTemplate jdbcTemplate) {
        this.useMySql = "mysql".equalsIgnoreCase(storage);
        this.dataDir = dataDir;
        this.jdbc = jdbcTemplate;
        log.info("Storage mode = {}", this.useMySql ? "MYSQL (tables in schema 'vrs')"
                : "FILE (JSON files in " + dataDir + ")");
    }

    @Bean
    public DataStore<User> userStore() {
        return useMySql
                ? new MySqlStore<>(jdbc, new UserMapping())
                : new JsonFileStore<>(Path.of(dataDir, "users.json"), User.class);
    }

    @Bean
    public DataStore<Vehicle> vehicleStore() {
        return useMySql
                ? new MySqlStore<>(jdbc, new VehicleMapping())
                : new JsonFileStore<>(Path.of(dataDir, "vehicles.json"), Vehicle.class);
    }

    @Bean
    public DataStore<Booking> bookingStore() {
        return useMySql
                ? new MySqlStore<>(jdbc, new BookingMapping())
                : new JsonFileStore<>(Path.of(dataDir, "bookings.json"), Booking.class);
    }

    @Bean
    public DataStore<Payment> paymentStore() {
        return useMySql
                ? new MySqlStore<>(jdbc, new PaymentMapping())
                : new JsonFileStore<>(Path.of(dataDir, "payments.json"), Payment.class);
    }

    @Bean
    public DataStore<Review> reviewStore() {
        return useMySql
                ? new MySqlStore<>(jdbc, new ReviewMapping())
                : new JsonFileStore<>(Path.of(dataDir, "reviews.json"), Review.class);
    }
}
