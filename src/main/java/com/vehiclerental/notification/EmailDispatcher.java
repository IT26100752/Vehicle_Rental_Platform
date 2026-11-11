package com.vehiclerental.notification;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The one {@link EmailService} the application uses.
 *
 * Responsibilities (EMAIL HANDLING):
 *  1. write every message to the email log (so the admin can see sent / failed mail)
 *  2. hand it to the active {@link MailTransport} - real SMTP mail is sent on a background thread so a slow
 *     mail server never makes a booking or a payment wait
 *  3. if the transport fails, save the message to data/outbox instead and record the error
 *  4. never throw: a mail problem must not break a booking, a payment or a registration
 */
@Service
public class EmailDispatcher implements EmailService {

    private static final System.Logger LOG = System.getLogger(EmailDispatcher.class.getName());

    private final MailTransport transport;
    private final MailTransport fallback;
    private final EmailLogRepository logs;
    private final boolean async;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread t = new Thread(runnable, "email-sender");
        t.setDaemon(true);
        return t;
    });

    public EmailDispatcher(MailTransport transport, EmailLogRepository logs,
                           @Value("${app.data-dir:data}") String dataDir,
                           @Value("${app.mail.async:true}") boolean async) {
        this.transport = transport;
        this.fallback = transport instanceof FileMailTransport ? transport : new FileMailTransport(dataDir);
        this.logs = logs;
        this.async = async;
    }

    @Override
    public EmailStatus send(EmailMessage message) {
        if (message.getTo().isBlank()) {
            LOG.log(System.Logger.Level.WARNING, "Email \"" + message.getSubject() + "\" skipped: no recipient");
            return EmailStatus.FAILED;
        }
        EmailLog log = logs.create(EmailLog.of(message, transport.name()));
        if (async && transport.isRealMail()) {
            Long logId = log.getId();
            worker.submit(() -> deliver(message, logId));
            return EmailStatus.QUEUED;
        }
        return deliver(message, log.getId());
    }

    @Override
    public boolean isDeliveringRealMail() {
        return transport.isRealMail();
    }

    /** Sends a previously failed email again (plain text only - attachments are not kept in the log). */
    public EmailStatus resend(Long logId) {
        Optional<EmailLog> found = logs.findById(logId);
        if (found.isEmpty()) {
            return EmailStatus.FAILED;
        }
        EmailLog old = found.get();
        EmailMessage message = new EmailMessage(old.getToAddress(), old.getSubject(), old.getBody(), null,
                old.getType(), null);
        return deliver(message, old.getId());
    }

    private EmailStatus deliver(EmailMessage message, Long logId) {
        EmailStatus status;
        String error = null;
        try {
            transport.deliver(message);
            status = transport.isRealMail() ? EmailStatus.SENT : EmailStatus.SAVED;
        } catch (RuntimeException ex) {
            error = ex.getMessage();
            LOG.log(System.Logger.Level.WARNING, "Email to " + message.getTo() + " failed (" + error
                    + "), saving to outbox");
            try {
                fallback.deliver(message);
                status = EmailStatus.SAVED;
            } catch (RuntimeException second) {
                status = EmailStatus.FAILED;
                error = error + " / outbox: " + second.getMessage();
            }
        }
        final EmailStatus finalStatus = status;
        final String finalError = error;
        logs.findById(logId).ifPresent(log -> {
            log.setStatus(finalStatus);
            log.setErrorMessage(finalError);
            log.setAttempts(log.getAttempts() + 1);
            logs.update(log);
        });
        return status;
    }

    @PreDestroy
    void shutdown() {
        worker.shutdown();
    }
}
