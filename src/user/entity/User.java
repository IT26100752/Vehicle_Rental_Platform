package user.entity;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.common.entity.Identifiable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * USER  (base class of the user hierarchy)
 * ----------------------------------------
 * OOP concepts demonstrated by this class
 *  1. ENCAPSULATION      - all fields are private, only reachable through
 *                          getters / setters.
 *  2. INFORMATION HIDING - the password is stored HASHED. Even the views
 *                          never see it.
 *  3. INHERITANCE        - CustomerUser and AdminUser extend this class.
 *  4. POLYMORPHISM       - describeRole() is overridden by both subclasses.
 *
 * The @JsonTypeInfo annotations tell Jackson which concrete subclass to create
 * when it reads users.json back from disk; in MySQL mode the "type" column of
 * the users table plays the same role.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdminUser.class,    name = "ADMIN"),
        @JsonSubTypes.Type(value = CustomerUser.class, name = "CUSTOMER")
})
public abstract class User implements Identifiable {

    private int id;
    private String fullName;
    private String username;
    /** INFORMATION HIDING: never plain text - always a SHA-256 hash. */
    private String passwordHash;
    private String email;
    private String phone;

    protected User() {
        // needed by Jackson when it deserialises the file
    }

    protected User(int id, String fullName, String username, String passwordHash,
                   String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.phone = phone;
    }

    /* POLYMORPHISM - each subclass tells the UI something different. */
    public abstract String describeRole();

    /** Permission check - overridden by AdminUser. */
    public boolean canManageUsers() {
        return false;
    }

    /**
     * The hash IS stored (file or table) - that is the whole point of hashing:
     * even someone who opens the storage cannot recover the plain password.
     * No Thymeleaf template ever renders this value.
     */
    @JsonProperty
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /* ------------------------- getters / setters ---------------------- */
    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return describeRole() + " #" + id + " - " + fullName + " (" + username + ")";
    }
}
