package com.vehiclerental.driver.entity;

import com.vehiclerental.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One driver sent on one booking. The secret {@code token} is put in the notification email so the driver can open
 * the trip page and confirm it without an account.
 */
public class DriverAssignment extends BaseEntity {

    private Long bookingId;
    private Long driverId;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;
    private String token;
    private LocalDateTime notifiedAt;
    private LocalDateTime acknowledgedAt;

    public boolean isLive() {
        return status != AssignmentStatus.CANCELLED;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status == null ? AssignmentStatus.ASSIGNED : status; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(LocalDateTime notifiedAt) { this.notifiedAt = notifiedAt; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
