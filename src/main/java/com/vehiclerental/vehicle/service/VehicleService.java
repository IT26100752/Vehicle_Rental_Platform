package com.vehiclerental.vehicle.service;

import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.vehicle.dto.VehicleFilter;
import com.vehiclerental.vehicle.dto.VehicleForm;
import com.vehiclerental.vehicle.entity.*;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;
import com.vehiclerental.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Vehicle CRUD plus search / filter. */
@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    // ------------------------------------------------------------ Create

    public Vehicle create(VehicleForm form) {
        if (repository.findByRegistrationNo(form.getRegistrationNo()).isPresent()) {
            throw new BusinessRuleException("A vehicle with that registration number already exists");
        }
        Vehicle vehicle = build(form);
        return repository.create(vehicle);
    }

    // -------------------------------------------------------------- Read

    public Vehicle findById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Vehicle", id));
    }

    public List<Vehicle> findAll() {
        List<Vehicle> all = repository.findAll();
        all.sort(Comparator.comparing(Vehicle::getId));
        return all;
    }

    public long count() {
        return repository.count();
    }

    public List<Vehicle> search(VehicleFilter filter) {
        String k = filter.getKeyword() == null ? "" : filter.getKeyword().trim().toLowerCase();
        Comparator<Vehicle> order = switch (filter.getSort() == null ? "newest" : filter.getSort()) {
            case "price_asc" -> Comparator.comparing(Vehicle::getPricePerDay);
            case "price_desc" -> Comparator.comparing(Vehicle::getPricePerDay).reversed();
            case "seats" -> Comparator.comparingInt(Vehicle::getSeatingCapacity).reversed();
            default -> Comparator.comparing(Vehicle::getId).reversed();
        };
        return repository.findAll().stream()
                .filter(v -> filter.getType() == null || v.getType() == filter.getType())
                .filter(v -> filter.getFuelType() == null || v.getFuelType() == filter.getFuelType())
                .filter(v -> filter.getMinSeats() == null || v.getSeatingCapacity() >= filter.getMinSeats())
                .filter(v -> filter.getMaxPrice() == null || v.getPricePerDay().compareTo(filter.getMaxPrice()) <= 0)
                .filter(v -> k.isEmpty()
                        || v.getModel().toLowerCase().contains(k)
                        || v.getRegistrationNo().toLowerCase().contains(k))
                .sorted(order)
                .toList();
    }

    // ------------------------------------------------------------ Update

    public Vehicle update(Long id, VehicleForm form) {
        Vehicle existing = findById(id);
        repository.findByRegistrationNo(form.getRegistrationNo())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessRuleException("Another vehicle already uses that registration number");
                });
        Vehicle updated = build(form);          // new object so a Car can become a Van
        updated.setId(existing.getId());
        updated.setCreatedAt(existing.getCreatedAt());
        return repository.update(updated);
    }

    public Vehicle updateStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = findById(id);
        vehicle.setStatus(status);
        return repository.update(vehicle);
    }

    // ------------------------------------------------------------ Delete

    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw ResourceNotFoundException.of("Vehicle", id);
        }
    }

    // ---------------------------------------------------------- internals

    /** Default real photo for each type when the admin does not choose one. */
    public static final java.util.Map<VehicleType, String> DEFAULT_IMAGE = java.util.Map.of(
            VehicleType.CAR, "/images/photos/car.jpg", VehicleType.VAN, "/images/photos/van.jpg",
            VehicleType.BIKE, "/images/photos/bike.jpg", VehicleType.TRUCK, "/images/photos/truck.jpg");

    /** Photos the admin can pick from in the form (label -> path). */
    public static final Map<String, String> IMAGE_PRESETS = new LinkedHashMap<>();
    static {
        IMAGE_PRESETS.put("Toyota Axio (car)", "/images/vehicles/axio.jpg");
        IMAGE_PRESETS.put("Nissan Leaf (car)", "/images/vehicles/leaf.jpg");
        IMAGE_PRESETS.put("Silver sedan (car)", "/images/photos/car.jpg");
        IMAGE_PRESETS.put("Toyota KDH (van)", "/images/vehicles/kdh.jpg");
        IMAGE_PRESETS.put("Nissan Caravan (van)", "/images/vehicles/caravan.jpg");
        IMAGE_PRESETS.put("White van (van)", "/images/photos/van.jpg");
        IMAGE_PRESETS.put("Yamaha FZ (bike)", "/images/vehicles/fz.jpg");
        IMAGE_PRESETS.put("Sport bike (bike)", "/images/photos/bike.jpg");
        IMAGE_PRESETS.put("Mitsubishi Canter (truck)", "/images/vehicles/canter.jpg");
        IMAGE_PRESETS.put("Box truck (truck)", "/images/photos/truck.jpg");
    }

    private Vehicle build(VehicleForm form) {
        Vehicle vehicle = VehicleRepository.create(form.getType());   // polymorphic creation
        vehicle.setBrand(form.getBrand() == null ? "" : form.getBrand().trim());
        vehicle.setModel(form.getModel());
        vehicle.setYear(form.getYear() == null ? java.time.Year.now().getValue() : form.getYear());
        vehicle.setTransmission(form.getTransmission() == null ? Transmission.AUTOMATIC : form.getTransmission());
        vehicle.setRegistrationNo(form.getRegistrationNo());
        vehicle.setPricePerDay(form.getPricePerDay());
        vehicle.setFuelType(form.getFuelType());
        vehicle.setSeatingCapacity(form.getSeatingCapacity());
        vehicle.setStatus(form.getStatus());
        vehicle.setDescription(form.getDescription());
        int spec = form.getSpec() == null ? 0 : form.getSpec();
        if (vehicle instanceof Car c) {
            c.setDoors(spec);
            c.setBootLitres(form.getBoot() == null ? 0 : form.getBoot());
        } else if (vehicle instanceof Van c) {
            c.setCargoKg(spec);
            c.setAirConditioned(form.isFlag());
        } else if (vehicle instanceof Bike c) {
            c.setEngineCc(spec);
            c.setHelmetProvided(form.isFlag());
        } else if (vehicle instanceof Truck c) {
            c.setMaxLoadKg(spec);
            c.setBodyKind(form.getBodyKind() == null || form.getBodyKind().isBlank() ? "Box" : form.getBodyKind().trim());
        }
        String image = form.getImageUrl();
        vehicle.setImageUrl(image == null || image.isBlank() ? DEFAULT_IMAGE.get(form.getType()) : image);
        return vehicle;
    }
}
