package com.vehiclerental.complaint.entity;

import com.vehiclerental.common.BaseEntity;

/** One message in a complaint conversation. authorRole is "ADMIN" or "CUSTOMER". */
public class ComplaintReply extends BaseEntity {

    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER = "CUSTOMER";

    private Long complaintId;
    private String authorRole = CUSTOMER;
    private String authorName;
    private String message;

    public boolean isFromAdmin() { return ADMIN.equals(authorRole); }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole == null ? CUSTOMER : authorRole; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
