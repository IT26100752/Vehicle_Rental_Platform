package com.vehiclerental.driver.dto;

import com.vehiclerental.driver.entity.Driver;
import com.vehiclerental.driver.entity.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Admin add / edit driver form. */
public class DriverForm {

    private Long id;
    @NotBlank(message = "Enter the driver's name")
    @Size(min = 2, max = 80, message = "Name must be 2 - 80 characters")
    private String fullName;
    @NotBlank(message = "Enter a phone number")
    @Pattern(regexp = "^\\+?[0-9 \\-]{9,16}$", message = "Enter a valid phone number")
    private String phone;
    @NotBlank(message = "Enter an email address (the trip details are sent here)")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$", message = "Enter a valid email address")
    private String email;
    @NotBlank(message = "Enter the driving licence number")
    @Size(max = 20, message = "Licence number is too long")
    private String licenceNo;
    @Size(max = 80, message = "Languages text is too long")
    private String languages = "Sinhala, English";
    @NotNull(message = "Choose a status")
    private DriverStatus status = DriverStatus.AVAILABLE;

    public static DriverForm from(Driver d) {
        DriverForm f = new DriverForm();
        f.id = d.getId(); f.fullName = d.getFullName(); f.phone = d.getPhone(); f.email = d.getEmail();
        f.licenceNo = d.getLicenceNo(); f.languages = d.getLanguages(); f.status = d.getStatus();
        return f;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLicenceNo() { return licenceNo; }
    public void setLicenceNo(String licenceNo) { this.licenceNo = licenceNo; }
    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }
    public DriverStatus getStatus() { return status; }
    public void setStatus(DriverStatus status) { this.status = status; }
}
