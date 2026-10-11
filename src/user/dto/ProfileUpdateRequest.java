package user.dto;

/**
 * Data carried by the profile edit form.
 */
public class ProfileUpdateRequest {

    private String fullName;
    private String email;
    private String phone;
    private String nic;
    private String address;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
