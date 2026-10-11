package user.entity;

/**
 * INHERITANCE - a CustomerUser IS-A User.
 * It only adds what is unique to a customer and re-uses everything else.
 */
public class CustomerUser extends User {

    private String nic;            // National ID
    private String address;
    private boolean premium;       // premium customers get rental discounts

    public CustomerUser() {
    }

    public CustomerUser(int id, String fullName, String username, String passwordHash,
                        String email, String phone, String nic, String address, boolean premium) {
        super(id, fullName, username, passwordHash, email, phone);
        this.nic = nic;
        this.address = address;
        this.premium = premium;
    }

    /* POLYMORPHISM - overriding the abstract method from User */
    @Override
    public String describeRole() {
        return premium ? "Premium Customer" : "Regular Customer";
    }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }
}
