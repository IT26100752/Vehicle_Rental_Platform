package com.vehiclerental.user.dto;

import com.vehiclerental.user.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Admin editing a customer. */
public class UserEditForm {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 80, message = "Name must be 2 - 80 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$", message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9 \\-]{9,16}$", message = "Enter a valid phone number")
    private String phone;

    @Size(max = 160, message = "Address is too long")
    private String address;

    private UserStatus status = UserStatus.ACTIVE;
    private com.vehiclerental.user.entity.CustomerTier tier = com.vehiclerental.user.entity.CustomerTier.STANDARD;
    private boolean emailVerified = true;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public com.vehiclerental.user.entity.CustomerTier getTier() { return tier; }
    public void setTier(com.vehiclerental.user.entity.CustomerTier tier) { this.tier = tier; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}
