package com.vehiclerental.complaint.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.complaint.entity.Complaint;
import com.vehiclerental.complaint.entity.ComplaintCategory;
import com.vehiclerental.complaint.entity.ComplaintPriority;
import com.vehiclerental.complaint.entity.ComplaintStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

/** Stores complaints in data/complaints.txt */
@Repository
public class ComplaintRepository extends FileRepository<Complaint> {

    public ComplaintRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "complaints.txt"));
    }

    public List<Complaint> findByCustomerId(Long customerId) {
        return findWhere(c -> customerId.equals(c.getCustomerId()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|customerId|customerName|bookingId|category|subject|description|status|priority|resolutionNote|resolvedAt";
    }

    @Override
    protected String[] toFields(Complaint c) {
        return new String[]{text(c.getCustomerId()), text(c.getCustomerName()), text(c.getBookingId()),
                c.getCategory().name(), text(c.getSubject()), text(c.getDescription()), c.getStatus().name(),
                c.getPriority().name(), text(c.getResolutionNote()), text(c.getResolvedAt())};
    }

    @Override
    protected Complaint fromFields(String[] f) {
        Complaint c = new Complaint();
        c.setCustomerId(lng(f, 0));
        c.setCustomerName(str(f, 1));
        c.setBookingId(lng(f, 2));
        c.setCategory(enumOf(ComplaintCategory.class, f, 3, ComplaintCategory.OTHER));
        c.setSubject(str(f, 4));
        c.setDescription(str(f, 5));
        c.setStatus(enumOf(ComplaintStatus.class, f, 6, ComplaintStatus.OPEN));
        c.setPriority(enumOf(ComplaintPriority.class, f, 7, ComplaintPriority.MEDIUM));
        c.setResolutionNote(strOrNull(f, 8));
        c.setResolvedAt(dateTime(f, 9));
        return c;
    }
}
