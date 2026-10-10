package com.vehiclerental.vehicle.service;

import com.vehiclerental.common.service.AbstractCrudService;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;
import com.vehiclerental.vehicle.repository.VehicleRepository;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MEMBER 2 - Vehicle Management service.
 */
@Service
public class VehicleService extends AbstractCrudService<Vehicle> {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        super(vehicleRepository);
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> search(String keyword, VehicleType type) {
        return vehicleRepository.search(keyword, type);
    }

    public List<Vehicle> available() {
        return vehicleRepository.findAvailable();
    }

    /** Called by BookingService - ABSTRACTION: other modules never touch the repo. */
    public boolean setStatus(int vehicleId, VehicleStatus status) {
        return findById(vehicleId)
                .map(v -> {
                    v.setStatus(status);
                    return update(v);
                })
                .orElse(false);
    }

    /**
     * POLYMORPHISM demo used on the dashboard: we iterate over the abstract
     * type Vehicle, but capacityInfo() resolves to the concrete subclass
     * implementation at runtime (Car vs Van vs Bike vs Truck).
     */
    public Map<String, Long> fleetSummaryByType() {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (VehicleType type : VehicleType.values()) {
            summary.put(type.label(), findAll().stream()
                    .filter(v -> v.getType() == type).count());
        }
        return summary;
    }
}
