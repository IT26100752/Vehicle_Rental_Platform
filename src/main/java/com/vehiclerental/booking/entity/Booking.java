package com.vehiclerental.booking.entity;

import com.vehiclerental.common.BaseEntity;
import com.vehiclerental.common.DateRules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * One rental. Money fields are private and only change through the service,
 * which keeps total / fine consistent (ENCAPSULATION / information hiding).
 */
public class Booking extends BaseEntity {

    private Long customerId;
    private Long vehicleId;
    /** Snapshot so history still reads well if the vehicle is later removed. */
    private String vehicleModel;
    private String vehicleRegNo;

    private LocalDate pickupDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;

    private String renterName;
    private String nic;
    private String sex;
    private int age;
    private String licenceId;

    private LocalTime pickupTime;
    private Long pickupLocationId;
    private String pickupLocationName;
    private Long dropLocationId;
    private String dropLocationName;
    /** Optional: hotel / street / landmark where the driver should collect the customer. */
    private String pickupAddress;

    private boolean withDriver;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    /** The driver the customer asked for when ordering (optional) and any note for the driver. */
    private Long preferredDriverId;
    private String driverNote;

    private BigDecimal pricePerDay = BigDecimal.ZERO;
    /** Itemised parts of the price (they add up to totalAmount). */
    private BigDecimal baseAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal driverFee = BigDecimal.ZERO;
    private BigDecimal oneWayFee = BigDecimal.ZERO;
    private String pricingNote;
    /** Promo code used on this booking and how much it took off (already part of discountAmount). */
    private String promoCode;
    private BigDecimal promoDiscount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** Unpaid bookings keep the vehicle only until this moment. */
    private LocalDateTime holdExpiresAt;
    private long lateDays;
    private BigDecimal fineAmount = BigDecimal.ZERO;

    private BookingStatus status = BookingStatus.PENDING_PAYMENT;
    private boolean docsVerified;
    private String verifiedBy;
    private String conditionNote;

    // ------------------------------------------------------------ behaviour

    public String getReference() {
        if (getId() == null) {
            return "BK-NEW";
        }
        int year = getCreatedAt() == null ? LocalDate.now().getYear() : getCreatedAt().getYear();
        return String.format("BK-%d-%05d", year, getId());
    }

    public long getRentalDays() {
        return DateRules.rentalDays(pickupDate, returnDate);
    }

    /** Rental amount plus any late fine. */
    public BigDecimal getTotalPayable() {
        return totalAmount.add(fineAmount);
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    /** Customers may cancel an unpaid booking any time, or a paid one until the pickup date. */
    public boolean isCancellableByCustomer(LocalDate today) {
        return status == BookingStatus.PENDING_PAYMENT || status == BookingStatus.AWAITING_VERIFICATION
                || (status == BookingStatus.CONFIRMED && today.isBefore(pickupDate));
    }

    /** A driver is wanted but nobody has been assigned yet. */
    public boolean isDriverPending() {
        return withDriver && driverId == null && status != BookingStatus.CANCELLED && status != BookingStatus.COMPLETED;
    }

    public boolean isOneWay() {
        return pickupLocationId != null && dropLocationId != null && !pickupLocationId.equals(dropLocationId);
    }

    public boolean isOverdue(LocalDate today) {
        return status == BookingStatus.ACTIVE && today.isAfter(returnDate);
    }

    // ------------------------------------------------------ getters/setters

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    public void setVehicleRegNo(String vehicleRegNo) {
        this.vehicleRegNo = vehicleRegNo;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName == null ? null : renterName.trim();
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic == null ? null : nic.trim().toUpperCase();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(String licenceId) {
        this.licenceId = licenceId == null ? null : licenceId.trim().toUpperCase();
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getLateDays() {
        return lateDays;
    }

    public void setLateDays(long lateDays) {
        this.lateDays = lateDays;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public boolean isDocsVerified() {
        return docsVerified;
    }

    public void setDocsVerified(boolean docsVerified) {
        this.docsVerified = docsVerified;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getConditionNote() {
        return conditionNote;
    }

    public void setConditionNote(String conditionNote) {
        this.conditionNote = conditionNote;
    }

    public LocalTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalTime pickupTime) { this.pickupTime = pickupTime; }
    public Long getPickupLocationId() { return pickupLocationId; }
    public void setPickupLocationId(Long pickupLocationId) { this.pickupLocationId = pickupLocationId; }
    public String getPickupLocationName() { return pickupLocationName; }
    public void setPickupLocationName(String pickupLocationName) { this.pickupLocationName = pickupLocationName; }
    public Long getDropLocationId() { return dropLocationId; }
    public void setDropLocationId(Long dropLocationId) { this.dropLocationId = dropLocationId; }
    public String getDropLocationName() { return dropLocationName; }
    public void setDropLocationName(String dropLocationName) { this.dropLocationName = dropLocationName; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress == null || pickupAddress.isBlank() ? null : pickupAddress.trim(); }
    public boolean isWithDriver() { return withDriver; }
    public void setWithDriver(boolean withDriver) { this.withDriver = withDriver; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public Long getPreferredDriverId() { return preferredDriverId; }
    public void setPreferredDriverId(Long preferredDriverId) { this.preferredDriverId = preferredDriverId; }
    public String getDriverNote() { return driverNote; }
    public void setDriverNote(String driverNote) {
        this.driverNote = driverNote == null || driverNote.isBlank() ? null : driverNote.trim();
    }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getDriverFee() { return driverFee; }
    public void setDriverFee(BigDecimal driverFee) { this.driverFee = driverFee; }
    public BigDecimal getOneWayFee() { return oneWayFee; }
    public void setOneWayFee(BigDecimal oneWayFee) { this.oneWayFee = oneWayFee; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public BigDecimal getPromoDiscount() { return promoDiscount; }
    public void setPromoDiscount(BigDecimal promoDiscount) { this.promoDiscount = promoDiscount == null ? BigDecimal.ZERO : promoDiscount; }
    public String getPricingNote() { return pricingNote; }
    public void setPricingNote(String pricingNote) { this.pricingNote = pricingNote; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }
}
