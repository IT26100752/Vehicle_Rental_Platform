package com.vehiclerental.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Real email through any SMTP server (Gmail, Brevo, Mailtrap ...). Switched on with app.mail.enabled=true.
 * Sends a multipart message: plain text + HTML + attachments (the PDF receipt).
 * If delivery fails, {@link EmailDispatcher} saves the message to data/outbox instead.
 */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpMailTransport implements MailTransport {

    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;

    public SmtpMailTransport(JavaMailSender mailSender,
                             @Value("${app.mail.from:}") String from,
                             @Value("${app.mail.from-name:DriveLanka Rentals}") String fromName) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
    }

    @Override
    public void deliver(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            if (!from.isBlank()) {
                helper.setFrom(from, fromName);
            }
            helper.setTo(message.getTo());
            helper.setSubject(message.getSubject());
            if (message.hasHtml()) {
                helper.setText(message.getTextBody(), message.getHtmlBody());
            } else {
                helper.setText(message.getTextBody(), false);
            }
            for (EmailAttachment attachment : message.getAttachments()) {
                helper.addAttachment(attachment.getFilename(),
                        new ByteArrayResource(attachment.getContent()), attachment.getContentType());
            }
            mailSender.send(mime);
        } catch (MessagingException | UnsupportedEncodingException | MailException ex) {
            throw new MailDeliveryException("SMTP delivery failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String name() {
        return "SMTP";
    }

    @Override
    public boolean isRealMail() {
        return true;
    }
}
