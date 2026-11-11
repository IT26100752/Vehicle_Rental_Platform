package com.vehiclerental.location.dto;

import com.vehiclerental.location.entity.Location;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Admin add / edit location form. */
public class LocationForm {

    private Long id;
    @NotBlank(message = "Enter the location name")
    @Size(max = 60, message = "Name is too long")
    private String name;
    @NotBlank(message = "Enter the city")
    @Size(max = 40, message = "City is too long")
    private String city;
    @Size(max = 160, message = "Address is too long")
    private String address;
    @Size(max = 20, message = "Phone is too long")
    private String phone;
    private boolean active = true;
    @DecimalMin(value = "5.8", message = "Latitude must be inside Sri Lanka (5.8 to 10.0)")
    @DecimalMax(value = "10.0", message = "Latitude must be inside Sri Lanka (5.8 to 10.0)")
    private Double latitude;
    @DecimalMin(value = "79.4", message = "Longitude must be inside Sri Lanka (79.4 to 82.1)")
    @DecimalMax(value = "82.1", message = "Longitude must be inside Sri Lanka (79.4 to 82.1)")
    private Double longitude;

    public static LocationForm from(Location l) {
        LocationForm f = new LocationForm();
        f.id = l.getId(); f.name = l.getName(); f.city = l.getCity();
        f.address = l.getAddress(); f.phone = l.getPhone(); f.active = l.isActive();
        f.latitude = l.getLatitude(); f.longitude = l.getLongitude();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
