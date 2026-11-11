package com.vehiclerental.vehicle.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.vehicle.entity.Bike;
import com.vehiclerental.vehicle.entity.Car;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.Transmission;
import com.vehiclerental.vehicle.entity.Truck;
import com.vehiclerental.vehicle.entity.Van;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Stores vehicles in data/vehicles.txt. The first field is the type, which decides Car / Van / Bike / Truck.
 * attr1 / attr2 hold the two type-specific values (see {@link #toFields}); old rows without the new
 * columns still load because missing fields fall back to defaults.
 */
@Repository
public class VehicleRepository extends FileRepository<Vehicle> {

    public VehicleRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "vehicles.txt"));
    }

    public Optional<Vehicle> findByRegistrationNo(String registrationNo) {
        if (registrationNo == null) {
            return Optional.empty();
        }
        String wanted = registrationNo.trim().toUpperCase();
        return findFirst(v -> wanted.equals(v.getRegistrationNo()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|type|model|registrationNo|pricePerDay|fuelType|seatingCapacity|status|imageUrl|description"
                + "|brand|year|transmission|attr1|attr2   (attr1/attr2: Car=doors,bootLitres Van=cargoKg,airCon"
                + " Bike=engineCc,helmet Truck=maxLoadKg,bodyKind)";
    }

    @Override
    protected String[] toFields(Vehicle v) {
        String a1 = "";
        String a2 = "";
        if (v instanceof Car c) {
            a1 = String.valueOf(c.getDoors());
            a2 = String.valueOf(c.getBootLitres());
        } else if (v instanceof Van c) {
            a1 = String.valueOf(c.getCargoKg());
            a2 = String.valueOf(c.isAirConditioned());
        } else if (v instanceof Bike c) {
            a1 = String.valueOf(c.getEngineCc());
            a2 = String.valueOf(c.isHelmetProvided());
        } else if (v instanceof Truck c) {
            a1 = String.valueOf(c.getMaxLoadKg());
            a2 = c.getBodyKind();
        }
        return new String[]{v.getType().name(), v.getModel(), v.getRegistrationNo(),
                v.getPricePerDay().toPlainString(), v.getFuelType().name(),
                String.valueOf(v.getSeatingCapacity()), v.getStatus().name(),
                text(v.getImageUrl()), text(v.getDescription()),
                text(v.getBrand()), String.valueOf(v.getYear()), v.getTransmission().name(), a1, a2};
    }

    @Override
    protected Vehicle fromFields(String[] f) {
        VehicleType type = enumOf(VehicleType.class, f, 0, VehicleType.CAR);
        Vehicle v = create(type);                                    // polymorphic creation
        v.setModel(str(f, 1));
        v.setRegistrationNo(str(f, 2));
        v.setPricePerDay(decimal(f, 3));
        v.setFuelType(enumOf(FuelType.class, f, 4, FuelType.PETROL));
        v.setSeatingCapacity(integer(f, 5) == null ? 4 : integer(f, 5));
        v.setStatus(enumOf(VehicleStatus.class, f, 6, VehicleStatus.AVAILABLE));
        v.setImageUrl(strOrNull(f, 7));
        v.setDescription(strOrNull(f, 8));
        v.setBrand(str(f, 9));
        v.setYear(integer(f, 10) == null ? 0 : integer(f, 10));
        v.setTransmission(enumOf(Transmission.class, f, 11, Transmission.AUTOMATIC));
        Integer n1 = integerOrNull(str(f, 12));
        if (v instanceof Car c) {
            c.setDoors(n1 == null ? 4 : n1);
            Integer n2 = integerOrNull(str(f, 13));
            c.setBootLitres(n2 == null ? 400 : n2);
        } else if (v instanceof Van c) {
            c.setCargoKg(n1 == null ? 700 : n1);
            c.setAirConditioned(str(f, 13).isEmpty() || bool(f, 13));
        } else if (v instanceof Bike c) {
            c.setEngineCc(n1 == null ? 150 : n1);
            c.setHelmetProvided(str(f, 13).isEmpty() || bool(f, 13));
        } else if (v instanceof Truck c) {
            c.setMaxLoadKg(n1 == null ? 1000 : n1);
            c.setBodyKind(str(f, 13));
        }
        return v;
    }

    /** Factory used by the repository and by VehicleService: one place that maps a type to its subclass. */
    public static Vehicle create(VehicleType type) {
        return switch (type) {
            case VAN -> new Van();
            case BIKE -> new Bike();
            case TRUCK -> new Truck();
            default -> new Car();
        };
    }

    private static Integer integerOrNull(String s) {
        try {
            return s == null || s.isEmpty() ? null : Integer.valueOf(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
