package com.vehiclerental.location.service;

import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.location.dto.LocationForm;
import com.vehiclerental.location.entity.Location;
import com.vehiclerental.location.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** Pickup / drop-off locations: CRUD for the admin and the active list for the booking dropdowns. */
@Service
public class LocationService {

    private final LocationRepository repository;

    public LocationService(LocationRepository repository) {
        this.repository = repository;
    }

    public Location create(LocationForm form) {
        Location l = new Location();
        apply(l, form);
        return repository.create(l);
    }

    public Location findById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Location", id));
    }

    public List<Location> findAll() {
        return repository.findAll().stream().sorted(Comparator.comparing(Location::getName)).toList();
    }

    /** What the dropdowns show. */
    public List<Location> findActive() {
        return findAll().stream().filter(Location::isActive).toList();
    }

    /** Looks up a location the customer picked; it must exist and be active. */
    public Location requireActive(Long id, String what) {
        if (id == null) {
            throw new BusinessRuleException("Choose a " + what + " location");
        }
        Location l = findById(id);
        if (!l.isActive()) {
            throw new BusinessRuleException("The " + what + " location is no longer available");
        }
        return l;
    }

    public Location update(Long id, LocationForm form) {
        Location l = findById(id);
        apply(l, form);
        return repository.update(l);
    }

    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw ResourceNotFoundException.of("Location", id);
        }
    }

    /** A pin on the home page map. */
    public static class MapPin {
        private final String name, city, address, phone;
        private final double latitude, longitude;
        MapPin(Location l, double lat, double lng) {
            this.name = l.getName(); this.city = l.getCity(); this.address = l.getAddress(); this.phone = l.getPhone();
            this.latitude = lat; this.longitude = lng;
        }
        public String getName() { return name; }
        public String getCity() { return city; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    /** Well-known towns, used when an admin did not type coordinates (latitude, longitude). */
    private static final java.util.Map<String, double[]> TOWNS = java.util.Map.ofEntries(
            java.util.Map.entry("colombo", new double[]{6.9344, 79.8428}),
            java.util.Map.entry("katunayake", new double[]{7.1808, 79.8841}),
            java.util.Map.entry("negombo", new double[]{7.2083, 79.8358}),
            java.util.Map.entry("kandy", new double[]{7.2906, 80.6337}),
            java.util.Map.entry("galle", new double[]{6.0329, 80.2168}),
            java.util.Map.entry("matara", new double[]{5.9549, 80.5550}),
            java.util.Map.entry("jaffna", new double[]{9.6615, 80.0255}),
            java.util.Map.entry("trincomalee", new double[]{8.5874, 81.2152}),
            java.util.Map.entry("batticaloa", new double[]{7.7310, 81.6747}),
            java.util.Map.entry("anuradhapura", new double[]{8.3114, 80.4037}),
            java.util.Map.entry("kurunegala", new double[]{7.4863, 80.3647}),
            java.util.Map.entry("nuwara eliya", new double[]{6.9497, 80.7891}),
            java.util.Map.entry("ella", new double[]{6.8667, 81.0466}),
            java.util.Map.entry("badulla", new double[]{6.9934, 81.0550}),
            java.util.Map.entry("ratnapura", new double[]{6.6828, 80.3992}),
            java.util.Map.entry("hambantota", new double[]{6.1246, 81.1185}),
            java.util.Map.entry("dambulla", new double[]{7.8742, 80.6511}),
            java.util.Map.entry("sigiriya", new double[]{7.9570, 80.7603}),
            java.util.Map.entry("mirissa", new double[]{5.9483, 80.4716}),
            java.util.Map.entry("bentota", new double[]{6.4259, 79.9953}),
            java.util.Map.entry("hikkaduwa", new double[]{6.1395, 80.1063}));

    /** Active locations that can be placed on the map (own coordinates, or a known town). */
    public List<MapPin> mapPins() {
        java.util.List<MapPin> pins = new java.util.ArrayList<>();
        for (Location l : findActive()) {
            Double lat = l.getLatitude();
            Double lng = l.getLongitude();
            if (lat == null || lng == null) {
                double[] town = l.getCity() == null ? null : TOWNS.get(l.getCity().trim().toLowerCase());
                if (town == null) {
                    continue;
                }
                lat = town[0];
                lng = town[1];
            }
            pins.add(new MapPin(l, lat, lng));
        }
        return pins;
    }

    public long count() {
        return repository.count();
    }

    private void apply(Location l, LocationForm f) {
        l.setName(f.getName());
        l.setCity(f.getCity());
        l.setAddress(f.getAddress());
        l.setPhone(f.getPhone());
        l.setActive(f.isActive());
        l.setLatitude(f.getLatitude());
        l.setLongitude(f.getLongitude());
    }
}
