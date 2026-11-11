package com.vehiclerental.complaint.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.complaint.entity.ComplaintReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

/** Stores complaint conversation messages in data/complaint_replies.txt */
@Repository
public class ComplaintReplyRepository extends FileRepository<ComplaintReply> {

    public ComplaintReplyRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "complaint_replies.txt"));
    }

    public List<ComplaintReply> findByComplaintId(Long complaintId) {
        return findWhere(r -> complaintId.equals(r.getComplaintId()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|complaintId|authorRole|authorName|message";
    }

    @Override
    protected String[] toFields(ComplaintReply r) {
        return new String[]{text(r.getComplaintId()), r.getAuthorRole(), text(r.getAuthorName()), text(r.getMessage())};
    }

    @Override
    protected ComplaintReply fromFields(String[] f) {
        ComplaintReply r = new ComplaintReply();
        r.setComplaintId(lng(f, 0));
        r.setAuthorRole(str(f, 1));
        r.setAuthorName(str(f, 2));
        r.setMessage(str(f, 3));
        return r;
    }
}
