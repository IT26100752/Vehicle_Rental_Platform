package com.vehiclerental.notification;

import com.vehiclerental.common.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

/** Stores the email log in data/email_log.txt */
@Repository
public class EmailLogRepository extends FileRepository<EmailLog> {

    public EmailLogRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "email_log.txt"));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|toAddress|subject|type|status|transport|errorMessage|attachmentNames|attempts|body";
    }

    @Override
    protected String[] toFields(EmailLog l) {
        return new String[]{text(l.getToAddress()), text(l.getSubject()), l.getType().name(), l.getStatus().name(),
                text(l.getTransport()), text(l.getErrorMessage()), text(l.getAttachmentNames()),
                String.valueOf(l.getAttempts()), text(l.getBody())};
    }

    @Override
    protected EmailLog fromFields(String[] f) {
        EmailLog l = new EmailLog();
        l.setToAddress(str(f, 0));
        l.setSubject(str(f, 1));
        l.setType(enumOf(EmailType.class, f, 2, EmailType.GENERAL));
        l.setStatus(enumOf(EmailStatus.class, f, 3, EmailStatus.QUEUED));
        l.setTransport(strOrNull(f, 4));
        l.setErrorMessage(strOrNull(f, 5));
        l.setAttachmentNames(strOrNull(f, 6));
        l.setAttempts(integer(f, 7) == null ? 0 : integer(f, 7));
        l.setBody(strOrNull(f, 8));
        return l;
    }
}
