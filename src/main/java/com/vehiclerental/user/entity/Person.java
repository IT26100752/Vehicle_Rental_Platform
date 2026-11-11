package com.vehiclerental.user.entity;

import com.vehiclerental.common.BaseEntity;

/**
 * Abstract parent of everyone who can sign in (Customer, Admin).
 * ENCAPSULATION: fields are private; the password hash is only reachable through
 * the dedicated accessor and is never printed by toString().
 * POLYMORPHISM: {@link #getRole()} and {@link #isLoginAllowed()} behave differently per subclass.
 */
public abstract class Person extends BaseEntity {

    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;

    public abstract String getRole();

    /** Whether this account may sign in right now. */
    public boolean isLoginAllowed() {
        return true;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? null : fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + getId() + ", name=" + fullName + ", email=" + email + "}";
    }
}
