package com.vehiclerental.location.entity;

import com.vehiclerental.common.BaseEntity;

/** A branch / hub where vehicles can be picked up or dropped off (shown in the booking dropdowns). */
public class Location extends BaseEntity {

    private String name;
    private String city;
    private String address;
    private String phone;
    private boolean active = true;
    /** Optional map position (decimal degrees). Blank = the city name is used to find a place on the map. */
    private Double latitude;
    private Double longitude;

    /** "Colombo Fort - Colombo". */
    public String getLabel() {
        return city == null || city.isBlank() ? name : name + " - " + city;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name == null ? null : name.trim(); }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city == null ? null : city.trim(); }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address == null ? null : address.trim(); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone == null ? null : phone.trim(); }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
