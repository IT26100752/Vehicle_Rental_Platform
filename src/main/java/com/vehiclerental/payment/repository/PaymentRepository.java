package com.vehiclerental.payment.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.payment.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Stores payments in data/payments.txt. The first field (CARD / CASH / BANK / WALLET) decides the subclass.
 * detail1..3 hold the method specific values; columns after detail3 were added later (old rows still load).
 */
@Repository
public class PaymentRepository extends FileRepository<Payment> {

    public PaymentRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "payments.txt"));
    }

    public List<Payment> findByBookingId(Long bookingId) {
        return findWhere(p -> bookingId.equals(p.getBookingId()));
    }

    public List<Payment> findByCustomerId(Long customerId) {
        return findWhere(p -> customerId.equals(p.getCustomerId()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|method|bookingId|customerId|amount|kind|status|reference|detail1|detail2|detail3"
                + "|receiptNo|paidAt|failureReason|verifiedBy   (CARD: holder,last4,brand  CASH: receivedBy"
                + "  BANK: bankName,slipRef,transferDate  WALLET: provider,maskedMobile)";
    }

    @Override
    protected String[] toFields(Payment p) {
        String code;
        String d1 = "";
        String d2 = "";
        String d3 = "";
        if (p instanceof CardPayment c) {
            code = "CARD";
            d1 = text(c.getCardHolder());
            d2 = text(c.getCardLast4());
            d3 = text(c.getCardBrand());
        } else if (p instanceof BankTransferPayment b) {
            code = "BANK";
            d1 = text(b.getBankName());
            d2 = text(b.getSlipReference());
            d3 = text(b.getTransferDate());
        } else if (p instanceof WalletPayment w) {
            code = "WALLET";
            d1 = text(w.getProvider());
            d2 = text(w.getMobileMasked());
        } else {
            code = "CASH";
            d1 = text(((CashPayment) p).getReceivedBy());
        }
        return new String[]{code, text(p.getBookingId()), text(p.getCustomerId()), p.getAmount().toPlainString(),
                p.getKind().name(), p.getStatus().name(), text(p.getReference()), d1, d2, d3,
                text(p.getReceiptNo()), text(p.getPaidAt()), text(p.getFailureReason()), text(p.getVerifiedBy())};
    }

    @Override
    protected Payment fromFields(String[] f) {
        Payment p;
        switch (str(f, 0)) {
            case "CARD" -> {
                CardPayment c = new CardPayment();
                c.setCardHolder(str(f, 7));
                c.setCardLast4(str(f, 8));
                c.setCardBrand(str(f, 9));
                p = c;
            }
            case "BANK" -> {
                BankTransferPayment b = new BankTransferPayment();
                b.setBankName(str(f, 7));
                b.setSlipReference(str(f, 8));
                String d = str(f, 9);
                b.setTransferDate(d.isEmpty() ? null : LocalDate.parse(d));
                p = b;
            }
            case "WALLET" -> {
                WalletPayment w = new WalletPayment();
                w.setProvider(str(f, 7));
                w.setMobileMasked(str(f, 8));
                p = w;
            }
            default -> {
                CashPayment c = new CashPayment();
                c.setReceivedBy(str(f, 7));
                p = c;
            }
        }
        p.setBookingId(lng(f, 1));
        p.setCustomerId(lng(f, 2));
        p.setAmount(decimal(f, 3));
        p.setKind(enumOf(PaymentKind.class, f, 4, PaymentKind.RENTAL));
        p.setStatus(enumOf(PaymentStatus.class, f, 5, PaymentStatus.PAID));
        p.setReference(str(f, 6));
        p.setReceiptNo(strOrNull(f, 10));
        p.setPaidAt(dateTime(f, 11));
        p.setFailureReason(strOrNull(f, 12));
        p.setVerifiedBy(strOrNull(f, 13));
        return p;
    }
}
