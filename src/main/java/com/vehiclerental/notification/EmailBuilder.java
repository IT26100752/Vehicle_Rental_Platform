package com.vehiclerental.notification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a branded HTML email AND its plain-text twin from the same pieces, so every message in the
 * system looks the same.   Example:
 * <pre>
 *   EmailMessage m = templates.builder("Booking confirmed")
 *           .greeting("Hello Nimal,")
 *           .paragraph("Thank you! Your booking is confirmed.")
 *           .fact("Booking ID", "BK-2026-00001")
 *           .button("View booking", "/bookings/1")
 *           .build("nimal@example.com", EmailType.BOOKING);
 * </pre>
 */
public final class EmailBuilder {

    private final String title;
    private final String baseUrl;
    private final String brand;

    private String greeting;
    private final List<String[]> blocks = new ArrayList<>();   // {kind, a, b}
    private final Map<String, String> facts = new LinkedHashMap<>();

    EmailBuilder(String title, String baseUrl, String brand) {
        this.title = title;
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        this.brand = brand;
    }

    public EmailBuilder greeting(String text) {
        this.greeting = text;
        return this;
    }

    public EmailBuilder paragraph(String text) {
        flushFacts();
        blocks.add(new String[]{"p", text, null});
        return this;
    }

    /** A highlighted box, for example a one-time code. */
    public EmailBuilder highlight(String label, String value) {
        flushFacts();
        blocks.add(new String[]{"hl", label, value});
        return this;
    }

    public EmailBuilder fact(String label, String value) {
        facts.put(label, value == null || value.isBlank() ? "-" : value);
        return this;
    }

    public EmailBuilder facts(Map<String, String> rows) {
        rows.forEach(this::fact);
        return this;
    }

    /** A call-to-action button. {@code path} may be a full URL or a path inside the application. */
    public EmailBuilder button(String label, String path) {
        flushFacts();
        String url = path != null && path.startsWith("http") ? path : baseUrl + (path == null ? "" : path);
        blocks.add(new String[]{"btn", label, url});
        return this;
    }

    public EmailBuilder note(String text) {
        flushFacts();
        blocks.add(new String[]{"note", text, null});
        return this;
    }

    public EmailMessage build(String to, EmailType type) {
        flushFacts();
        return new EmailMessage(to, title, buildText(), buildHtml(), type, null);
    }

    // ----------------------------------------------------------------- rendering

    private void flushFacts() {
        if (facts.isEmpty()) {
            return;
        }
        StringBuilder packed = new StringBuilder();
        facts.forEach((k, v) -> packed.append(k).append('\u0001').append(v).append('\u0002'));
        blocks.add(new String[]{"facts", packed.toString(), null});
        facts.clear();
    }

    private String buildText() {
        StringBuilder sb = new StringBuilder();
        if (greeting != null) {
            sb.append(greeting).append("\n\n");
        }
        for (String[] b : blocks) {
            switch (b[0]) {
                case "p", "note" -> sb.append(b[1]).append("\n\n");
                case "hl" -> sb.append(b[1]).append(": ").append(b[2]).append("\n\n");
                case "btn" -> sb.append(b[1]).append(": ").append(b[2]).append("\n\n");
                case "facts" -> {
                    for (String row : b[1].split("\u0002")) {
                        if (!row.isEmpty()) {
                            String[] kv = row.split("\u0001", 2);
                            sb.append(String.format("%-18s: %s%n", kv[0], kv.length > 1 ? kv[1] : ""));
                        }
                    }
                    sb.append('\n');
                }
                default -> { }
            }
        }
        sb.append(brand);
        return sb.toString();
    }

    private String buildHtml() {
        StringBuilder h = new StringBuilder(2048);
        h.append("<!DOCTYPE html><html><body style=\"margin:0;background:#eef1f3;font-family:Segoe UI,Arial,sans-serif;color:#1c262d\">");
        h.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\" style=\"padding:24px 12px\">");
        h.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background:#ffffff;border-radius:14px;overflow:hidden\">");
        h.append("<tr><td style=\"background:#0f1417;padding:20px 28px\"><span style=\"color:#f2b134;font-size:20px;font-weight:800;letter-spacing:.3px\">")
                .append(esc(brand)).append("</span></td></tr>");
        h.append("<tr><td style=\"padding:28px\">");
        h.append("<h1 style=\"margin:0 0 14px;font-size:22px;color:#0f1417\">").append(esc(title)).append("</h1>");
        if (greeting != null) {
            h.append("<p style=\"margin:0 0 14px;font-size:15px;line-height:1.55\">").append(esc(greeting)).append("</p>");
        }
        for (String[] b : blocks) {
            switch (b[0]) {
                case "p" -> h.append("<p style=\"margin:0 0 14px;font-size:15px;line-height:1.55\">")
                        .append(esc(b[1]).replace("\n", "<br>")).append("</p>");
                case "note" -> h.append("<p style=\"margin:0 0 14px;padding:12px 14px;background:#fff7e0;border-left:4px solid #f2b134;border-radius:6px;font-size:14px;line-height:1.5\">")
                        .append(esc(b[1]).replace("\n", "<br>")).append("</p>");
                case "hl" -> h.append("<div style=\"margin:6px 0 18px;text-align:center\"><div style=\"font-size:12px;letter-spacing:1px;text-transform:uppercase;color:#5c676d\">")
                        .append(esc(b[1])).append("</div><div style=\"display:inline-block;margin-top:6px;padding:12px 26px;background:#0f1417;color:#f2b134;font-size:32px;font-weight:800;letter-spacing:8px;border-radius:10px\">")
                        .append(esc(b[2])).append("</div></div>");
                case "btn" -> h.append("<p style=\"margin:8px 0 18px\"><a href=\"").append(esc(b[2]))
                        .append("\" style=\"display:inline-block;background:#f2b134;color:#1a1405;text-decoration:none;font-weight:700;padding:12px 22px;border-radius:9px\">")
                        .append(esc(b[1])).append("</a></p>");
                case "facts" -> {
                    h.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:4px 0 18px;border:1px solid #e1e6e9;border-radius:10px\">");
                    for (String row : b[1].split("\u0002")) {
                        if (!row.isEmpty()) {
                            String[] kv = row.split("\u0001", 2);
                            h.append("<tr><td style=\"padding:9px 14px;border-bottom:1px solid #eef1f3;font-size:14px;color:#5c676d;width:38%\">")
                                    .append(esc(kv[0])).append("</td><td style=\"padding:9px 14px;border-bottom:1px solid #eef1f3;font-size:14px;font-weight:600\">")
                                    .append(esc(kv.length > 1 ? kv[1] : "")).append("</td></tr>");
                        }
                    }
                    h.append("</table>");
                }
                default -> { }
            }
        }
        h.append("</td></tr><tr><td style=\"padding:16px 28px;background:#f6f8f9;font-size:12px;color:#7a868c\">")
                .append(esc(brand)).append(" - this is an automatic message, please do not reply.</td></tr>");
        h.append("</table></td></tr></table></body></html>");
        return h.toString();
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
