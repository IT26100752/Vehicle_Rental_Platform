package com.vehiclerental.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Rental details typed by the customer. */
public class BookingForm {

    @NotNull(message = "Vehicle is missing")
    private Long vehicleId;

    @NotNull(message = "Choose a pickup date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pickupDate;

    @NotNull(message = "Choose a return date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    @NotBlank(message = "Enter the renter's name as on the NIC")
    @Size(min = 2, max = 80, message = "Name must be 2 - 80 characters")
    private String renterName;

    @NotBlank(message = "Enter your NIC number")
    @Pattern(regexp = "^([0-9]{9}[VvXx]|[0-9]{12})$", message = "NIC must be 9 digits + V/X, or 12 digits")
    private String nic;

    @NotBlank(message = "Select sex")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Select Male, Female or Other")
    private String sex;

    @NotNull(message = "Enter your age")
    @Min(value = 18, message = "Renters must be at least 18 years old")
    @Max(value = 80, message = "Please contact us for renters above 80")
    private Integer age;

    /** Required only for a self-drive rental (checked in BookingService); empty is fine when a driver is hired. */
    @Pattern(regexp = "^$|^[A-Za-z0-9-]{6,15}$", message = "Licence ID must be 6 - 15 letters / digits")
    private String licenceId;

    /** false = self-drive, true = a driver comes with the vehicle. */
    private boolean withDriver;

    @NotNull(message = "Choose a pickup location")
    private Long pickupLocationId;

    @NotNull(message = "Choose a drop-off location")
    private Long dropLocationId;

    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$", message = "Choose a pickup time")
    private String pickupTime = "09:00";

    @Size(max = 160, message = "Address note is too long")
    private String pickupAddress;

    /** Optional (with-driver only): the driver the customer would like, and a note for the driver. */
    private Long preferredDriverId;

    @Size(max = 200, message = "The note for the driver is too long (200 characters)")
    private String driverNote;

    @AssertTrue(message = "Please confirm you will bring the original NIC and driving licence")
    private boolean documentsAcknowledged;

    /** Optional discount code typed by the customer. */
    @Size(max = 20, message = "The promo code is too long")
    private String promoCode;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getLicenceId() { return licenceId; }
    public void setLicenceId(String licenceId) { this.licenceId = licenceId; }
    public boolean isWithDriver() { return withDriver; }
    public void setWithDriver(boolean withDriver) { this.withDriver = withDriver; }
    public Long getPickupLocationId() { return pickupLocationId; }
    public void setPickupLocationId(Long pickupLocationId) { this.pickupLocationId = pickupLocationId; }
    public Long getDropLocationId() { return dropLocationId; }
    public void setDropLocationId(Long dropLocationId) { this.dropLocationId = dropLocationId; }
    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }
    public java.time.LocalTime getPickupTimeValue() {
        return pickupTime == null || pickupTime.isBlank() ? null : java.time.LocalTime.parse(pickupTime);
    }
    public Long getPreferredDriverId() { return preferredDriverId; }
    public void setPreferredDriverId(Long preferredDriverId) { this.preferredDriverId = preferredDriverId; }
    public String getDriverNote() { return driverNote; }
    public void setDriverNote(String driverNote) { this.driverNote = driverNote; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode == null || promoCode.isBlank() ? null : promoCode.trim().toUpperCase(); }
    public boolean isDocumentsAcknowledged() { return documentsAcknowledged; }
    public void setDocumentsAcknowledged(boolean documentsAcknowledged) { this.documentsAcknowledged = documentsAcknowledged; }
}
