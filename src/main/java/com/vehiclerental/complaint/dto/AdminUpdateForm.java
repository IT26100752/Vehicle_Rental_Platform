package com.vehiclerental.complaint.dto;

import com.vehiclerental.complaint.entity.ComplaintPriority;
import com.vehiclerental.complaint.entity.ComplaintStatus;

/** Admin changes status / priority and may leave a resolution note. */
public class AdminUpdateForm {

    private ComplaintStatus status;
    private ComplaintPriority priority;
    private String resolutionNote;

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }
    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}
