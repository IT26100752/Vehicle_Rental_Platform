package com.vehiclerental.booking.repository;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.common.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

/** Stores bookings in data/bookings.txt */
@Repository
public class BookingRepository extends FileRepository<Booking> {

    public BookingRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "bookings.txt"));
    }

    public List<Booking> findByCustomerId(Long customerId) {
        return findWhere(b -> customerId.equals(b.getCustomerId()));
    }

    public List<Booking> findByVehicleId(Long vehicleId) {
        return findWhere(b -> vehicleId.equals(b.getVehicleId()));
    }

    public List<Booking> findByStatus(BookingStatus status) {
        return findWhere(b -> b.getStatus() == status);
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|customerId|vehicleId|vehicleModel|vehicleRegNo|pickupDate|returnDate|actualReturnDate|"
                + "renterName|nic|sex|age|licenceId|pricePerDay|totalAmount|lateDays|fineAmount|status|"
                + "docsVerified|verifiedBy|conditionNote|pickupTime|pickupLocationId|pickupLocationName|dropLocationId|"
                + "dropLocationName|pickupAddress|withDriver|driverId|driverName|driverPhone|baseAmount|discountAmount|"
                + "driverFee|oneWayFee|pricingNote|holdExpiresAt|preferredDriverId|driverNote|promoCode|promoDiscount";
    }

    @Override
    protected String[] toFields(Booking b) {
        return new String[]{text(b.getCustomerId()), text(b.getVehicleId()), text(b.getVehicleModel()),
                text(b.getVehicleRegNo()), text(b.getPickupDate()), text(b.getReturnDate()),
                text(b.getActualReturnDate()), text(b.getRenterName()), text(b.getNic()), text(b.getSex()),
                String.valueOf(b.getAge()), text(b.getLicenceId()), b.getPricePerDay().toPlainString(),
                b.getTotalAmount().toPlainString(), String.valueOf(b.getLateDays()),
                b.getFineAmount().toPlainString(), b.getStatus().name(), String.valueOf(b.isDocsVerified()),
                text(b.getVerifiedBy()), text(b.getConditionNote()),
                text(b.getPickupTime()), text(b.getPickupLocationId()), text(b.getPickupLocationName()),
                text(b.getDropLocationId()), text(b.getDropLocationName()), text(b.getPickupAddress()),
                String.valueOf(b.isWithDriver()), text(b.getDriverId()), text(b.getDriverName()),
                text(b.getDriverPhone()), b.getBaseAmount().toPlainString(), b.getDiscountAmount().toPlainString(),
                b.getDriverFee().toPlainString(), b.getOneWayFee().toPlainString(), text(b.getPricingNote()),
                text(b.getHoldExpiresAt()), text(b.getPreferredDriverId()), text(b.getDriverNote()),
                text(b.getPromoCode()), b.getPromoDiscount().toPlainString()};
    }

    @Override
    protected Booking fromFields(String[] f) {
        Booking b = new Booking();
        b.setCustomerId(lng(f, 0));
        b.setVehicleId(lng(f, 1));
        b.setVehicleModel(str(f, 2));
        b.setVehicleRegNo(str(f, 3));
        b.setPickupDate(date(f, 4));
        b.setReturnDate(date(f, 5));
        b.setActualReturnDate(date(f, 6));
        b.setRenterName(str(f, 7));
        b.setNic(str(f, 8));
        b.setSex(str(f, 9));
        b.setAge(integer(f, 10) == null ? 0 : integer(f, 10));
        b.setLicenceId(str(f, 11));
        b.setPricePerDay(decimal(f, 12));
        b.setTotalAmount(decimal(f, 13));
        b.setLateDays(lng(f, 14) == null ? 0 : lng(f, 14));
        b.setFineAmount(decimal(f, 15));
        b.setStatus(enumOf(BookingStatus.class, f, 16, BookingStatus.PENDING_PAYMENT));
        b.setDocsVerified(bool(f, 17));
        b.setVerifiedBy(strOrNull(f, 18));
        b.setConditionNote(strOrNull(f, 19));
        // columns added later: old rows simply do not have them
        String time = str(f, 20);
        b.setPickupTime(time.isEmpty() ? null : java.time.LocalTime.parse(time));
        b.setPickupLocationId(lng(f, 21));
        b.setPickupLocationName(strOrNull(f, 22));
        b.setDropLocationId(lng(f, 23));
        b.setDropLocationName(strOrNull(f, 24));
        b.setPickupAddress(strOrNull(f, 25));
        b.setWithDriver(bool(f, 26));
        b.setDriverId(lng(f, 27));
        b.setDriverName(strOrNull(f, 28));
        b.setDriverPhone(strOrNull(f, 29));
        b.setBaseAmount(str(f, 30).isEmpty() ? b.getTotalAmount() : decimal(f, 30));
        b.setDiscountAmount(decimal(f, 31));
        b.setDriverFee(decimal(f, 32));
        b.setOneWayFee(decimal(f, 33));
        b.setPricingNote(strOrNull(f, 34));
        b.setHoldExpiresAt(dateTime(f, 35));
        b.setPreferredDriverId(lng(f, 36));          // added later: old rows do not have these two
        b.setDriverNote(strOrNull(f, 37));
        b.setPromoCode(strOrNull(f, 38));            // added later: old rows do not have these two
        b.setPromoDiscount(decimal(f, 39));
        return b;
    }
}
