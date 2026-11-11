package com.vehiclerental.payment.receipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Everything printed on a payment receipt. Built by ReceiptService from a Payment + its Booking,
 * rendered by templates/payment/receipt.html and by ReceiptPdfGenerator. Plain data holder (no logic).
 */
public class Receipt {

    private String receiptNo;
    private LocalDateTime issuedAt;
    /** PAID, PENDING VERIFICATION, REFUNDED or FAILED (upper case, ready to print). */
    private String statusLabel;
    /** PAID / PENDING / REFUNDED / FAILED - used for colours. */
    private String statusCode;

    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String bookingRef;
    private String vehicleName;
    private String vehicleRegNo;
    private String vehicleType;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private String pickupTime;
    private long days;
    private String pickupLocation;
    private String dropLocation;
    private boolean withDriver;
    private String driverName;
    private String driverPhone;

    private String paymentReference;
    private String paymentMethod;
    private String paymentDetails;
    /** "Rental" or "Late fine". */
    private String paymentKind;

    private List<ReceiptLine> lines = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private String note;

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }
    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }
    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getVehicleRegNo() { return vehicleRegNo; }
    public void setVehicleRegNo(String vehicleRegNo) { this.vehicleRegNo = vehicleRegNo; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }
    public long getDays() { return days; }
    public void setDays(long days) { this.days = days; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }
    public boolean isWithDriver() { return withDriver; }
    public void setWithDriver(boolean withDriver) { this.withDriver = withDriver; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }
    public String getPaymentKind() { return paymentKind; }
    public void setPaymentKind(String paymentKind) { this.paymentKind = paymentKind; }
    public List<ReceiptLine> getLines() { return lines; }
    public void setLines(List<ReceiptLine> lines) { this.lines = lines == null ? new ArrayList<>() : lines; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total == null ? BigDecimal.ZERO : total; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
