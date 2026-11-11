package com.vehiclerental.payment.receipt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure-Java (no library) PDF writer for payment receipts: A4, PDF 1.4, built-in Helvetica fonts.
 * Usage: {@code byte[] pdf = new ReceiptPdfGenerator().generate(receipt);}
 */
public class ReceiptPdfGenerator {

    private static final double W = 595, H = 842, M = 40;
    private static final double FOOTER_RESERVE = 60;

    private static final int[] HELV = {278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556, 1015,
            667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611,
            278, 278, 278, 469, 556, 333,
            556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556, 556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500,
            334, 260, 334, 584};
    private static final int[] HELV_B = {278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611, 975,
            722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778, 667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611,
            333, 278, 333, 584, 556, 333,
            556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611, 611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500,
            389, 280, 389, 584};

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    // ------------------------------------------------------------------ canvas

    private static final class Page {
        final StringBuilder c = new StringBuilder();
    }

    private final List<Page> pages = new ArrayList<>();
    private Page page;
    private double y;

    public byte[] generate(Receipt r) {
        pages.clear();
        newPage();
        header(r);
        y = 112;
        statusBadge(r);
        y = 158;
        infoBlocks(r);
        paymentBlock(r);
        table(r);
        totalBox(r);
        note(r);
        footers();
        return write();
    }

    private void newPage() {
        page = new Page();
        pages.add(page);
    }

    private void header(Receipt r) {
        rect(0, 0, W, 96, 0x0f1417);
        rect(0, 96, W, 3, 0xf2b134);
        text(M, 44, 22, true, 0xf2b134, nz(r.getCompanyName(), "DriveLanka Rentals"));
        double ly = 62;
        for (String s : new String[]{r.getCompanyAddress(), phoneEmail(r)}) {
            if (s != null && !s.isBlank()) {
                text(M, ly, 8.5, false, 0xb9c2c7, s);
                ly += 12;
            }
        }
        textRight(W - M, 32, 9, false, 0x9aa6ab, "PAYMENT RECEIPT");
        textRight(W - M, 50, 15, true, 0xffffff, nz(r.getReceiptNo(), "-"));
        textRight(W - M, 66, 9, false, 0xb9c2c7, r.getIssuedAt() == null ? "" : r.getIssuedAt().format(STAMP));
    }

    private void statusBadge(Receipt r) {
        String code = nz(r.getStatusCode(), "PAID").toUpperCase(Locale.ROOT);
        String label = nz(r.getStatusLabel(), code);
        int colour = switch (code) {
            case "PAID" -> 0x1f9d6b;
            case "PENDING" -> 0xe0a020;
            case "FAILED" -> 0xd6454a;
            default -> 0x6b7780;
        };
        double w = 54 + width(label, 11, true);
        double h = 28;
        rect(M, y, w, h, colour);
        // icon
        double cx = M + 18, cy = y + h / 2;
        page.c.append("1 1 1 RG 2 w 1 J 1 j\n");
        switch (code) {
            case "PAID" -> path(cx - 6, cy + 0, cx - 2, cy + 5, cx + 6, cy - 5);
            case "PENDING" -> {
                circle(cx, cy, 8);
                path(cx, cy - 5, cx, cy, cx + 4, cy + 3);
            }
            case "FAILED" -> {
                path(cx - 5, cy - 5, cx + 5, cy + 5);
                path(cx + 5, cy - 5, cx - 5, cy + 5);
            }
            default -> {
                path(cx - 6, cy - 2, cx + 5, cy - 2, cx + 5, cy + 5, cx - 3, cy + 5);
                path(cx - 4, cy - 6, cx - 6, cy - 2, cx - 2, cy + 1);
            }
        }
        text(M + 36, y + 18.5, 11, true, 0xffffff, label);
        textRight(W - M, y + 18, 9, false, 0x6b7780, nz(r.getPaymentKind(), "Rental") + " payment");
        y += h;
    }

    private void infoBlocks(Receipt r) {
        double colW = (W - 2 * M - 24) / 2;
        double x2 = M + colW + 24;
        List<String[]> left = new ArrayList<>();
        left.add(new String[]{"Name", r.getCustomerName()});
        left.add(new String[]{"Email", r.getCustomerEmail()});
        left.add(new String[]{"Phone", r.getCustomerPhone()});

        List<String[]> right = new ArrayList<>();
        right.add(new String[]{"Booking ref", r.getBookingRef()});
        right.add(new String[]{"Vehicle", nz(r.getVehicleName(), "") + (blank(r.getVehicleRegNo()) ? "" : " (" + r.getVehicleRegNo() + ")")});
        right.add(new String[]{"Type", r.getVehicleType()});
        right.add(new String[]{"Dates", fmt(r.getPickupDate()) + " to " + fmt(r.getReturnDate()) + " (" + r.getDays() + " day" + (r.getDays() == 1 ? "" : "s") + ")"});
        right.add(new String[]{"Pickup time", r.getPickupTime()});
        right.add(new String[]{"Pickup", r.getPickupLocation()});
        right.add(new String[]{"Drop-off", r.getDropLocation()});
        right.add(new String[]{"Driver", r.isWithDriver()
                ? nz(r.getDriverName(), "To be assigned") + (blank(r.getDriverPhone()) ? "" : " - " + r.getDriverPhone())
                : "Self-drive"});

        double top = y + 22;
        sectionLabel(M, top, "BILLED TO");
        sectionLabel(x2, top, "BOOKING");
        double ly = kv(left, M, top + 18, colW);
        double ry = kv(right, x2, top + 18, colW);
        y = Math.max(ly, ry) + 8;
        hline(y, 0xdfe4e7);
        y += 14;
    }

    private void paymentBlock(Receipt r) {
        sectionLabel(M, y + 8, "PAYMENT");
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Method", r.getPaymentMethod()});
        rows.add(new String[]{"Details", r.getPaymentDetails()});
        rows.add(new String[]{"Reference", r.getPaymentReference()});
        rows.add(new String[]{"Type", r.getPaymentKind()});
        y = kv(rows, M, y + 26, W - 2 * M) + 8;
    }

    private void table(Receipt r) {
        double xDesc = M + 8, xDetail = M + 175, xAmt = W - M - 8;
        double detailW = xAmt - 90 - xDetail;
        tableHead(xDesc, xDetail, xAmt);
        boolean zebra = false;
        for (ReceiptLine l : r.getLines()) {
            List<String> d1 = wrap(l.getLabel(), 9.5, true, 160);
            List<String> d2 = wrap(l.getDetail(), 8.5, false, detailW);
            int n = Math.max(d1.size(), d2.size());
            double rh = Math.max(24, n * 12 + 12);
            if (y + rh > H - FOOTER_RESERVE - 60) {
                newPage();
                y = M;
                tableHead(xDesc, xDetail, xAmt);
                zebra = false;
            }
            if (zebra) {
                rect(M, y, W - 2 * M, rh, 0xf3f5f6);
            }
            zebra = !zebra;
            int ink = l.isDiscount() ? 0x1a8a5a : 0x1c262d;
            double ty = y + 16;
            for (String s : d1) {
                text(xDesc, ty, 9.5, true, ink, s);
                ty += 12;
            }
            ty = y + 16;
            for (String s : d2) {
                text(xDetail, ty, 8.5, false, 0x5c676d, s);
                ty += 12;
            }
            textRight(xAmt, y + 16, 9.5, l.isDiscount(), ink, money(l.getAmount()));
            y += rh;
        }
        hline(y, 0xdfe4e7);
        y += 10;
    }

    private void tableHead(double xDesc, double xDetail, double xAmt) {
        rect(M, y, W - 2 * M, 22, 0x1c262d);
        text(xDesc, y + 15, 8.5, true, 0xffffff, "DESCRIPTION");
        text(xDetail, y + 15, 8.5, true, 0xffffff, "DETAIL");
        textRight(xAmt, y + 15, 8.5, true, 0xffffff, "AMOUNT (LKR)");
        y += 22;
    }

    private void totalBox(Receipt r) {
        if (y + 56 > H - FOOTER_RESERVE) {
            newPage();
            y = M;
        }
        double w = 250, h = 46, x = W - M - w;
        rect(x, y + 6, w, h, 0x0f1417);
        rect(x, y + 6, 4, h, 0xf2b134);
        text(x + 16, y + 34, 10, true, 0xb9c2c7, "TOTAL");
        String amount = "LKR " + money(r.getTotal());
        textRight(x + w - 14, y + 36, 17, true, 0xf2b134, amount);
        y += h + 20;
    }

    private void note(Receipt r) {
        if (blank(r.getNote())) {
            return;
        }
        List<String> lines = wrap(r.getNote(), 9, false, W - 2 * M - 24);
        double h = lines.size() * 12 + 16;
        if (y + h > H - FOOTER_RESERVE) {
            newPage();
            y = M;
        }
        rect(M, y, W - 2 * M, h, 0xfff7e0);
        rect(M, y, 3, h, 0xf2b134);
        double ty = y + 17;
        for (String s : lines) {
            text(M + 14, ty, 9, false, 0x4a3b10, s);
            ty += 12;
        }
        y += h + 10;
    }

    private void footers() {
        int n = pages.size();
        for (int i = 0; i < n; i++) {
            page = pages.get(i);
            hline(H - 52, 0xdfe4e7);
            text(M, H - 36, 9, true, 0x1c262d, "Thank you for choosing " + "our rental service.");
            text(M, H - 24, 8, false, 0x7a868c, "This receipt was generated automatically on " + LocalDateTime.now().format(STAMP)
                    + " and is valid without a signature.");
            textRight(W - M, H - 24, 8, false, 0x7a868c, "Page " + (i + 1) + " of " + n);
        }
    }

    // ---------------------------------------------------------------- helpers

    private double kv(List<String[]> rows, double x, double top, double colW) {
        double ty = top;
        double labelW = 70;
        for (String[] row : rows) {
            if (blank(row[1])) {
                continue;
            }
            text(x, ty, 8.5, false, 0x7a868c, row[0]);
            for (String s : wrap(row[1], 9.5, true, colW - labelW)) {
                text(x + labelW, ty, 9.5, true, 0x1c262d, s);
                ty += 13;
            }
            ty += 2;
        }
        return ty;
    }

    private void sectionLabel(double x, double ty, String s) {
        text(x, ty, 8, true, 0xc98f1f, s);
    }

    private void hline(double yy, int rgb) {
        page.c.append(rg(rgb, true)).append("0.8 w ").append(f(M)).append(' ').append(f(H - yy)).append(" m ")
                .append(f(W - M)).append(' ').append(f(H - yy)).append(" l S\n");
    }

    private void rect(double x, double yTop, double w, double h, int rgb) {
        page.c.append(rg(rgb, false)).append(f(x)).append(' ').append(f(H - yTop - h)).append(' ')
                .append(f(w)).append(' ').append(f(h)).append(" re f\n");
    }

    private void path(double... pts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pts.length; i += 2) {
            sb.append(f(pts[i])).append(' ').append(f(H - pts[i + 1])).append(i == 0 ? " m " : " l ");
        }
        page.c.append(sb).append("S\n");
    }

    private void circle(double cx, double cy, double r) {
        double k = 0.5523 * r, py = H - cy;
        page.c.append(f(cx + r)).append(' ').append(f(py)).append(" m ")
                .append(f(cx + r)).append(' ').append(f(py + k)).append(' ').append(f(cx + k)).append(' ').append(f(py + r)).append(' ').append(f(cx)).append(' ').append(f(py + r)).append(" c ")
                .append(f(cx - k)).append(' ').append(f(py + r)).append(' ').append(f(cx - r)).append(' ').append(f(py + k)).append(' ').append(f(cx - r)).append(' ').append(f(py)).append(" c ")
                .append(f(cx - r)).append(' ').append(f(py - k)).append(' ').append(f(cx - k)).append(' ').append(f(py - r)).append(' ').append(f(cx)).append(' ').append(f(py - r)).append(" c ")
                .append(f(cx + k)).append(' ').append(f(py - r)).append(' ').append(f(cx + r)).append(' ').append(f(py - k)).append(' ').append(f(cx + r)).append(' ').append(f(py)).append(" c S\n");
    }

    private void text(double x, double yTop, double size, boolean bold, int rgb, String s) {
        page.c.append("BT ").append(rg(rgb, false)).append(bold ? "/F2 " : "/F1 ").append(f(size)).append(" Tf ")
                .append(f(x)).append(' ').append(f(H - yTop)).append(" Td (").append(escape(s)).append(") Tj ET\n");
    }

    private void textRight(double xRight, double yTop, double size, boolean bold, int rgb, String s) {
        text(xRight - width(s, size, bold), yTop, size, bold, rgb, s);
    }

    private static String rg(int rgb, boolean stroke) {
        return f(((rgb >> 16) & 255) / 255.0) + " " + f(((rgb >> 8) & 255) / 255.0) + " " + f((rgb & 255) / 255.0)
                + (stroke ? " RG " : " rg ");
    }

    private static String f(double v) {
        String s = String.format(Locale.ROOT, "%.2f", v);
        return s;
    }

    private static String clean(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append(c >= 32 && c <= 126 ? c : (c == '\n' || c == '\t' ? ' ' : '?'));
        }
        return sb.toString();
    }

    private static String escape(String s) {
        return clean(s).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    static double width(String s, double size, boolean bold) {
        int[] t = bold ? HELV_B : HELV;
        double w = 0;
        for (char c : clean(s).toCharArray()) {
            w += t[c - 32];
        }
        return w * size / 1000.0;
    }

    private static List<String> wrap(String s, double size, boolean bold, double maxW) {
        List<String> out = new ArrayList<>();
        String t = clean(s).trim();
        if (t.isEmpty()) {
            out.add("");
            return out;
        }
        StringBuilder line = new StringBuilder();
        for (String word : t.split(" +")) {
            while (width(word, size, bold) > maxW && word.length() > 1) {
                int cut = word.length() - 1;
                while (cut > 1 && width(word.substring(0, cut), size, bold) > maxW) {
                    cut--;
                }
                if (line.length() > 0) {
                    out.add(line.toString());
                    line.setLength(0);
                }
                out.add(word.substring(0, cut));
                word = word.substring(cut);
            }
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (width(candidate, size, bold) <= maxW) {
                line.setLength(0);
                line.append(candidate);
            } else {
                out.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) {
            out.add(line.toString());
        }
        return out;
    }

    private static String money(BigDecimal a) {
        BigDecimal v = a == null ? BigDecimal.ZERO : a;
        String s = String.format(Locale.ROOT, "%,.2f", v.abs());
        return v.signum() < 0 ? "-" + s : s;
    }

    private static String fmt(LocalDate d) {
        return d == null ? "-" : d.format(DATE);
    }

    private static String phoneEmail(Receipt r) {
        String p = nz(r.getCompanyPhone(), "");
        String e = nz(r.getCompanyEmail(), "");
        return (p + (p.isEmpty() || e.isEmpty() ? "" : "  |  ") + e).trim();
    }

    private static String nz(String s, String d) {
        return blank(s) ? d : s;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    // -------------------------------------------------------------- PDF file

    private byte[] write() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            put(out, "%PDF-1.4\n%âãÏÓ\n");
            int n = pages.size();
            // objects: 1 catalog, 2 pages, 3 F1, 4 F2, then per page: page obj, content obj
            obj(out, offsets, "<< /Type /Catalog /Pages 2 0 R >>");
            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < n; i++) {
                kids.append(5 + 2 * i).append(" 0 R ");
            }
            obj(out, offsets, "<< /Type /Pages /Kids [" + kids + "] /Count " + n + " >>");
            obj(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
            obj(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
            for (int i = 0; i < n; i++) {
                int content = 6 + 2 * i;
                obj(out, offsets, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents "
                        + content + " 0 R >>");
                byte[] data = pages.get(i).c.toString().getBytes(StandardCharsets.ISO_8859_1);
                offsets.add(out.size());
                put(out, (offsets.size()) + " 0 obj\n<< /Length " + data.length + " >>\nstream\n");
                out.write(data);
                put(out, "\nendstream\nendobj\n");
            }
            int xref = out.size();
            put(out, "xref\n0 " + (offsets.size() + 1) + "\n0000000000 65535 f \n");
            for (int off : offsets) {
                put(out, String.format(Locale.ROOT, "%010d 00000 n \n", off));
            }
            put(out, "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R /Info << /Title (Payment receipt) /Producer (DriveLanka Rentals) >> >>\nstartxref\n"
                    + xref + "\n%%EOF\n");
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void obj(ByteArrayOutputStream out, List<Integer> offsets, String body) throws IOException {
        offsets.add(out.size());
        put(out, offsets.size() + " 0 obj\n" + body + "\nendobj\n");
    }

    private static void put(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }
}
