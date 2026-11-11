package com.vehiclerental.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Data typed on the registration form (validated before it reaches the service). */
public class RegisterRequest {

    @NotBlank(message = "Please enter your full name")
    @Size(min = 2, max = 80, message = "Name must be 2 - 80 characters")
    private String fullName;

    @NotBlank(message = "Please enter your email")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$", message = "Enter a valid email address, e.g. name@example.com")
    private String email;

    @NotBlank(message = "Please enter your phone number")
    @Pattern(regexp = "^\\+?[0-9 \\-]{9,16}$", message = "Enter a valid phone number, e.g. 0771234567")
    private String phone;

    @Size(max = 160, message = "Address is too long")
    private String address;

    @NotBlank(message = "Please choose a password")
    @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password needs at least one letter and one digit")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
