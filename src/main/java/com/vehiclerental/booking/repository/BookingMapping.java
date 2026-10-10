package com.vehiclerental.booking.repository;

import com.vehiclerental.booking.entity.Booking;
import com.vehiclerental.booking.entity.BookingStatus;
import com.vehiclerental.common.repository.TableMapping;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BookingMapping implements TableMapping<Booking> {

    @Override
    public String table() {
        return "bookings";
    }

    @Override
    public String columns() {
        return "id,user_id,vehicle_id,pickup_date,return_date,pickup_location,"
                + "return_location,total_amount,status,returned_date,fine";
    }

    @Override
    public Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("vehicle_id"),
                rs.getDate("pickup_date").toLocalDate(),
                rs.getDate("return_date").toLocalDate(),
                rs.getString("pickup_location"),
                rs.getString("return_location"),
                rs.getDouble("total_amount"),
                BookingStatus.valueOf(rs.getString("status")));
        Date returned = rs.getDate("returned_date");
        if (returned != null) {
            b.setReturnedDate(returned.toLocalDate());
        }
        b.setFine(rs.getDouble("fine"));
        return b;
    }

    @Override
    public Object[] args(Booking b) {
        return new Object[]{b.getId(), b.getUserId(), b.getVehicleId(),
                Date.valueOf(b.getPickupDate()), Date.valueOf(b.getReturnDate()),
                b.getPickupLocation(), b.getReturnLocation(), b.getTotalAmount(),
                b.getStatus().name(),
                b.getReturnedDate() == null ? null : Date.valueOf(b.getReturnedDate()),
                b.getFine()};
    }
}
