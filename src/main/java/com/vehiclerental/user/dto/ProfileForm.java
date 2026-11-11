package com.vehiclerental.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Editable part of a customer's own profile. */
public class ProfileForm {

    @NotBlank(message = "Please enter your full name")
    @Size(min = 2, max = 80, message = "Name must be 2 - 80 characters")
    private String fullName;

    @NotBlank(message = "Please enter your phone number")
    @Pattern(regexp = "^\\+?[0-9 \\-]{9,16}$", message = "Enter a valid phone number, e.g. 0771234567")
    private String phone;

    @Size(max = 160, message = "Address is too long")
    private String address;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
