package com.vehiclerental.vehicle.repository;

import com.vehiclerental.common.repository.AbstractFileRepository;
import com.vehiclerental.common.repository.DataStore;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Member 2's repository - everything about the vehicles storage.
 */
@Repository
public class VehicleRepository extends AbstractFileRepository<Vehicle> {

    public VehicleRepository(DataStore<Vehicle> store) {
        super(store);
    }

    public Optional<Vehicle> findByVehicleNumber(String vehicleNumber) {
        return findAll().stream()
                .filter(v -> v.getVehicleNumber().equalsIgnoreCase(vehicleNumber))
                .findFirst();
    }

    public List<Vehicle> findAvailable() {
        return findAll().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .toList();
    }

    /** Search by brand / model / plate, optionally filtered by type. */
    public List<Vehicle> search(String keyword, VehicleType type) {
        return findAll().stream()
                .filter(v -> type == null || v.getType() == type)
                .filter(v -> keyword == null || keyword.isBlank()
                        || v.getBrand().toLowerCase().contains(keyword.toLowerCase())
                        || v.getModel().toLowerCase().contains(keyword.toLowerCase())
                        || v.getVehicleNumber().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}
