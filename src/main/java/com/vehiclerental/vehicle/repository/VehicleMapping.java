package com.vehiclerental.vehicle.repository;

import com.vehiclerental.common.repository.TableMapping;
import com.vehiclerental.vehicle.entity.Bike;
import com.vehiclerental.vehicle.entity.Car;
import com.vehiclerental.vehicle.entity.FuelType;
import com.vehiclerental.vehicle.entity.Transmission;
import com.vehiclerental.vehicle.entity.Truck;
import com.vehiclerental.vehicle.entity.Van;
import com.vehiclerental.vehicle.entity.Vehicle;
import com.vehiclerental.vehicle.entity.VehicleStatus;
import com.vehiclerental.vehicle.entity.VehicleType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * vehicles table. The "vehicle_type" column selects the subclass
 * (Car / Van / Bike / Truck) exactly like the JSON "vehicleType" property.
 */
public class VehicleMapping implements TableMapping<Vehicle> {

    @Override
    public String table() {
        return "vehicles";
    }

    @Override
    public String columns() {
        return "id,vehicle_type,vehicle_number,brand,model,year,price_per_day,"
                + "fuel_type,transmission,seats,status,image_url,"
                + "door_count,boot_capacity_liters,cargo_capacity_kg,has_ac,"
                + "engine_cc,helmet_provided,max_load_kg,body_kind";
    }

    @Override
    public Vehicle map(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String number = rs.getString("vehicle_number");
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        int year = rs.getInt("year");
        double price = rs.getDouble("price_per_day");
        FuelType fuel = FuelType.valueOf(rs.getString("fuel_type"));
        Transmission gear = Transmission.valueOf(rs.getString("transmission"));
        int seats = rs.getInt("seats");
        VehicleStatus status = VehicleStatus.valueOf(rs.getString("status"));
        String image = rs.getString("image_url");

        return switch (VehicleType.valueOf(rs.getString("vehicle_type"))) {
            case CAR -> new Car(id, number, brand, model, year, price, fuel, gear, seats,
                    status, image, rs.getInt("door_count"), rs.getInt("boot_capacity_liters"));
            case VAN -> new Van(id, number, brand, model, year, price, fuel, gear, seats,
                    status, image, rs.getDouble("cargo_capacity_kg"), rs.getBoolean("has_ac"));
            case BIKE -> new Bike(id, number, brand, model, year, price, fuel, gear, seats,
                    status, image, rs.getInt("engine_cc"), rs.getBoolean("helmet_provided"));
            case TRUCK -> new Truck(id, number, brand, model, year, price, fuel, gear, seats,
                    status, image, rs.getDouble("max_load_kg"), rs.getString("body_kind"));
        };
    }

    @Override
    public Object[] args(Vehicle v) {
        Integer doorCount = null, boot = null, engineCc = null;
        Double cargo = null, maxLoad = null;
        Boolean hasAc = null, helmet = null;
        String bodyKind = null;
        if (v instanceof Car c) {
            doorCount = c.getDoorCount();
            boot = c.getBootCapacityLiters();
        } else if (v instanceof Van van) {
            cargo = van.getCargoCapacityKg();
            hasAc = van.isHasAc();
        } else if (v instanceof Bike b) {
            engineCc = b.getEngineCc();
            helmet = b.isHelmetProvided();
        } else if (v instanceof Truck t) {
            maxLoad = t.getMaxLoadKg();
            bodyKind = t.getBodyKind();
        }
        return new Object[]{v.getId(), v.getType().name(), v.getVehicleNumber(), v.getBrand(),
                v.getModel(), v.getYear(), v.getPricePerDay(), v.getFuelType().name(),
                v.getTransmission().name(), v.getSeats(), v.getStatus().name(), v.getImageUrl(),
                doorCount, boot, cargo, hasAc, engineCc, helmet, maxLoad, bodyKind};
    }
}
