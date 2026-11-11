package com.vehiclerental.payment.service;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.service.BookingService;
import com.vehiclerental.notification.EmailAttachment;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentKind;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;
import com.vehiclerental.payment.receipt.Receipt;
import com.vehiclerental.payment.receipt.ReceiptLine;
import com.vehiclerental.payment.receipt.ReceiptPdfGenerator;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Turns a Payment (plus its booking and customer) into a Receipt, and the Receipt into a PDF. */
@Service
public class ReceiptService {

    private final BookingService bookings;
    private final UserService users;
    private final com.vehiclerental.vehicle.service.VehicleService vehicles;
    private final ReceiptPdfGenerator pdf = new ReceiptPdfGenerator();
    private final String companyName;
    private final String companyAddress;
    private final String companyPhone;
    private final String companyEmail;

    public ReceiptService(BookingService bookings, UserService users,
                          com.vehiclerental.vehicle.service.VehicleService vehicles,
                          @Value("${app.company.name:DriveLanka Rentals (Pvt) Ltd}") String companyName,
                          @Value("${app.company.address:42 Galle Road, Colombo 03, Sri Lanka}") String companyAddress,
                          @Value("${app.company.phone:+94 11 234 5678}") String companyPhone,
                          @Value("${app.company.email:hello@drivelanka.example}") String companyEmail) {
        this.bookings = bookings;
        this.users = users;
        this.vehicles = vehicles;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
    }

    /** "RCT-2026-00012": built from the year and the payment id, so it is unique and easy to quote. */
    public static String receiptNumberFor(Payment p) {
        return String.format("RCT-%d-%05d", p.getPaidAt().getYear(), p.getId());
    }

    public Receipt build(Payment p) {
        Booking b = bookings.findById(p.getBookingId());
        Customer c = users.findById(p.getCustomerId());
        Receipt r = new Receipt();
        r.setReceiptNo(p.getReceiptNo() != null ? p.getReceiptNo() : "PROFORMA-" + p.getReference());
        r.setIssuedAt(p.getPaidAt());
        r.setStatusCode(switch (p.getStatus()) {
            case PAID -> "PAID";
            case PENDING_VERIFICATION -> "PENDING";
            case REFUNDED -> "REFUNDED";
            default -> "FAILED";
        });
        r.setStatusLabel(p.getStatus().getLabel());
        r.setCompanyName(companyName);
        r.setCompanyAddress(companyAddress);
        r.setCompanyPhone(companyPhone);
        r.setCompanyEmail(companyEmail);
        r.setCustomerName(c.getFullName());
        r.setCustomerEmail(c.getEmail());
        r.setCustomerPhone(c.getPhone());
        r.setBookingRef(b.getReference());
        r.setVehicleName(b.getVehicleModel());
        r.setVehicleRegNo(b.getVehicleRegNo());
        try {
            r.setVehicleType(vehicles.findById(b.getVehicleId()).getType().getLabel());
        } catch (RuntimeException gone) {                 // the vehicle was deleted after the booking
            r.setVehicleType("");
        }
        r.setPickupDate(b.getPickupDate());
        r.setReturnDate(b.getReturnDate());
        r.setPickupTime(b.getPickupTime() == null ? "" : b.getPickupTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        r.setDays(b.getRentalDays());
        r.setPickupLocation(b.getPickupLocationName());
        r.setDropLocation(b.getDropLocationName());
        r.setWithDriver(b.isWithDriver());
        r.setDriverName(b.getDriverName());
        r.setDriverPhone(b.getDriverPhone());
        r.setPaymentReference(p.getReference());
        r.setPaymentMethod(p.getMethod());
        r.setPaymentDetails(p.getMethodDetails());
        r.setPaymentKind(p.getKind().getLabel());
        r.setLines(linesFor(p, b));
        r.setTotal(p.getAmount());
        r.setNote(noteFor(p));
        return r;
    }

    public byte[] pdf(Payment p) {
        return pdf.generate(build(p));
    }

    public String fileName(Payment p) {
        return (p.getReceiptNo() != null ? p.getReceiptNo() : p.getReference()) + ".pdf";
    }

    public EmailAttachment attachment(Payment p) {
        return EmailAttachment.pdf(fileName(p), pdf(p));
    }

    private static List<ReceiptLine> linesFor(Payment p, Booking b) {
        List<ReceiptLine> lines = new ArrayList<>();
        if (p.getKind() == PaymentKind.FINE) {
            lines.add(new ReceiptLine("Late return fine", b.getLateDays() + " day(s) late", p.getAmount()));
            return lines;
        }
        if (b.getBaseAmount() == null || b.getBaseAmount().signum() == 0) {      // booking made before itemised pricing
            lines.add(new ReceiptLine("Vehicle rental", b.getRentalDays() + " day(s)", p.getAmount()));
            return lines;
        }
        lines.add(new ReceiptLine("Vehicle rental", b.getRentalDays() + " day(s)", b.getBaseAmount()));
        if (b.getDiscountAmount() != null && b.getDiscountAmount().signum() > 0) {
            lines.add(new ReceiptLine("Discount", b.getPricingNote() == null ? "" : b.getPricingNote(),
                    b.getDiscountAmount().negate()));
        }
        if (b.getDriverFee() != null && b.getDriverFee().signum() > 0) {
            lines.add(new ReceiptLine("Driver", "Professional driver for the whole trip", b.getDriverFee()));
        }
        if (b.getOneWayFee() != null && b.getOneWayFee().signum() > 0) {
            lines.add(new ReceiptLine("One-way fee", "Drop-off at a different location", b.getOneWayFee()));
        }
        return lines;
    }

    private static String noteFor(Payment p) {
        return switch (p.getStatus()) {
            case PENDING_VERIFICATION -> p.getMethodType() == PaymentMethod.CASH
                    ? "This is not a final receipt. Please pay the amount in cash at our counter; "
                        + "you will receive the paid receipt by email once our staff confirm it."
                    : "This is not a final receipt. The bank transfer is being verified; "
                        + "you will receive the paid receipt by email once it is approved.";
            case REFUNDED -> "This payment has been refunded to the original payment method.";
            case PAID -> "Please bring your original NIC (and driving licence for self drive) when collecting the vehicle.";
            default -> "This payment was not completed.";
        };
    }
}
