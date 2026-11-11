package com.vehiclerental.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Development mail transport: every "sent" email is written to data/outbox/ as a .txt file
 * (plus a .html copy and the attachments), so mail can be shown in a viva without an SMTP server.
 * Also used as the safety net when real SMTP fails.
 */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class FileMailTransport implements MailTransport {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final Path outbox;

    public FileMailTransport(@Value("${app.data-dir:data}") String dataDir) {
        this.outbox = Path.of(dataDir, "outbox");
    }

    @Override
    public void deliver(EmailMessage message) {
        try {
            Files.createDirectories(outbox);
            String stamp = STAMP.format(LocalDateTime.now());
            String safeName = message.getTo().replaceAll("[^A-Za-z0-9@._-]", "_");
            String base = stamp + "_" + safeName;

            StringBuilder text = new StringBuilder();
            text.append("To: ").append(message.getTo()).append('\n');
            text.append("Subject: ").append(message.getSubject()).append('\n');
            text.append("Type: ").append(message.getType().getLabel()).append('\n');
            text.append("Date: ").append(LocalDateTime.now()).append('\n');
            for (EmailAttachment a : message.getAttachments()) {
                text.append("Attachment: ").append(a.getFilename()).append(" (").append(a.getSize()).append(" bytes)\n");
            }
            text.append('\n').append(message.getTextBody()).append('\n');
            Files.writeString(outbox.resolve(base + ".txt"), text.toString(), StandardCharsets.UTF_8);

            if (message.hasHtml()) {
                Files.writeString(outbox.resolve(base + ".html"), message.getHtmlBody(), StandardCharsets.UTF_8);
            }
            if (!message.getAttachments().isEmpty()) {
                Path folder = outbox.resolve("attachments");
                Files.createDirectories(folder);
                for (EmailAttachment a : message.getAttachments()) {
                    String file = a.getFilename().replaceAll("[^A-Za-z0-9._-]", "_");
                    Files.write(folder.resolve(base + "_" + file), a.getContent());
                }
            }
        } catch (IOException ex) {
            throw new MailDeliveryException("Could not write the email to the outbox: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String name() {
        return "OUTBOX";
    }

    @Override
    public boolean isRealMail() {
        return false;
    }
}
