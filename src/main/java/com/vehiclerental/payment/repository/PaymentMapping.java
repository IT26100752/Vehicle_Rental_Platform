package com.vehiclerental.payment.repository;

import com.vehiclerental.common.repository.TableMapping;
import com.vehiclerental.payment.entity.Payment;
import com.vehiclerental.payment.entity.PaymentMethod;
import com.vehiclerental.payment.entity.PaymentStatus;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentMapping implements TableMapping<Payment> {

    @Override
    public String table() {
        return "payments";
    }

    @Override
    public String columns() {
        return "id,booking_id,amount,method,payment_date,status,transaction_reference";
    }

    @Override
    public Payment map(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("booking_id"),
                rs.getDouble("amount"),
                PaymentMethod.valueOf(rs.getString("method")),
                rs.getDate("payment_date").toLocalDate(),
                PaymentStatus.valueOf(rs.getString("status")),
                rs.getString("transaction_reference"));
    }

    @Override
    public Object[] args(Payment p) {
        return new Object[]{p.getId(), p.getBookingId(), p.getAmount(),
                p.getMethod().name(), Date.valueOf(p.getPaymentDate()),
                p.getStatus().name(), p.getTransactionReference()};
    }
}
