package com.vehiclerental.driver.entity;

import com.vehiclerental.common.BaseEntity;

/** A professional driver who can be sent with a "with driver" booking. */
public class Driver extends BaseEntity {

    private String fullName;
    private String phone;
    private String email;
    private String licenceNo;
    private String languages = "Sinhala, English";
    private DriverStatus status = DriverStatus.AVAILABLE;

    public boolean isAssignable() {
        return status == DriverStatus.AVAILABLE;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName == null ? null : fullName.trim(); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone == null ? null : phone.trim(); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim().toLowerCase(); }
    public String getLicenceNo() { return licenceNo; }
    public void setLicenceNo(String licenceNo) { this.licenceNo = licenceNo == null ? null : licenceNo.trim().toUpperCase(); }
    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status == null ? DriverStatus.AVAILABLE : status; }
}
