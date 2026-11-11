package com.vehiclerental.notification;

import com.vehiclerental.common.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

/** Stores in-app notifications in data/notifications.txt */
@Repository
public class NotificationRepository extends FileRepository<Notification> {

    public NotificationRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "notifications.txt"));
    }

    public List<Notification> findForAdmins() {
        return findWhere(Notification::isForAdmins);
    }

    public List<Notification> findForCustomer(Long customerId) {
        return findWhere(n -> !n.isForAdmins() && customerId.equals(n.getRecipientId()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|recipientRole|recipientId|type|title|message|link|read";
    }

    @Override
    protected String[] toFields(Notification n) {
        return new String[]{n.getRecipientRole(), text(n.getRecipientId()), n.getType().name(),
                text(n.getTitle()), text(n.getMessage()), text(n.getLink()), String.valueOf(n.isRead())};
    }

    @Override
    protected Notification fromFields(String[] f) {
        Notification n = new Notification();
        n.setRecipientRole(str(f, 0));
        n.setRecipientId(lng(f, 1));
        n.setType(enumOf(NotificationType.class, f, 2, NotificationType.SYSTEM));
        n.setTitle(str(f, 3));
        n.setMessage(str(f, 4));
        n.setLink(strOrNull(f, 5));
        n.setRead(bool(f, 6));
        return n;
    }
}
