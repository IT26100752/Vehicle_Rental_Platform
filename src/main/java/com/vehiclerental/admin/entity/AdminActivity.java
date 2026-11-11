package com.vehiclerental.admin.entity;

import com.vehiclerental.common.BaseEntity;

/** One line of the staff activity log: who did what and when. */
public class AdminActivity extends BaseEntity {

    private String adminName;
    private String action;
    private String detail;

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}
