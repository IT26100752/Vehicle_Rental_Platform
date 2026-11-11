package com.vehiclerental.vehicle.service;

import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.review.service.ReviewService;
import com.vehiclerental.vehicle.dto.VehicleCard;
import com.vehiclerental.vehicle.dto.VehicleFilter;
import com.vehiclerental.vehicle.entity.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;

/** Builds the vehicle cards shown to customers (status + rating on top of the raw vehicle). */
@Service
public class VehicleCatalogService {

    private final VehicleService vehicles;
    private final BookingService bookings;
    private final ReviewService reviews;

    public VehicleCatalogService(VehicleService vehicles, BookingService bookings, ReviewService reviews) {
        this.vehicles = vehicles;
        this.bookings = bookings;
        this.reviews = reviews;
    }

    public VehicleCard card(Vehicle vehicle) {
        return new VehicleCard(vehicle, bookings.displayStatus(vehicle),
                reviews.averageRating(vehicle.getId()), reviews.count(vehicle.getId()));
    }

    public List<VehicleCard> cards(List<Vehicle> list) {
        return list.stream().map(this::card).toList();
    }

    /** Search with the filter; if both dates are chosen, keep only vehicles free for the whole period. */
    public List<VehicleCard> search(VehicleFilter filter) {
        List<Vehicle> found = vehicles.search(filter);
        if (filter.getPickupDate() != null && filter.getReturnDate() != null) {
            found = found.stream()
                    .filter(v -> bookings.checkAvailability(v.getId(), filter.getPickupDate(), filter.getReturnDate(), null)
                            .isAvailable())
                    .toList();
        }
        return cards(found);
    }

    /** A handful of vehicles for the home page / dashboard: bookable ones first. */
    public List<VehicleCard> featured(int limit) {
        return cards(vehicles.findAll()).stream()
                .filter(VehicleCard::isBookable)
                .limit(limit)
                .toList();
    }
}
