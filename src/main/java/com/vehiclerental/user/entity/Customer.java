package com.vehiclerental.user.entity;

/** A registered customer who can browse, book, pay and review. */
public class Customer extends Person {

    private String address;
    /** Last rental details - kept so the booking form can be pre-filled next time. */
    private String nic;
    private String sex;
    private Integer age;
    private String licenceId;
    private UserStatus status = UserStatus.ACTIVE;
    /** False until the 6-digit email code is confirmed. Defaults to true so seeded / old rows stay usable. */
    private boolean emailVerified = true;
    /** Premium members get cheaper rates and gentler late fees (see PricingService / LateFeePolicy). */
    private CustomerTier tier = CustomerTier.STANDARD;

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public boolean isLoginAllowed() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic == null ? null : nic.trim().toUpperCase();
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(String licenceId) {
        this.licenceId = licenceId == null ? null : licenceId.trim().toUpperCase();
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status == null ? UserStatus.ACTIVE : status;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public void setTier(CustomerTier tier) {
        this.tier = tier == null ? CustomerTier.STANDARD : tier;
    }
}
