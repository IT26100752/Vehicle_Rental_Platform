package com.vehiclerental.admin.entity;

import com.vehiclerental.user.entity.Person;

/** Staff account that signs in with a username and manages the system. A MANAGER also manages the other admins. */
public class Admin extends Person {

    private String username;
    private AdminLevel level = AdminLevel.ADMIN;
    private java.time.LocalDateTime lastLoginAt;

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim().toLowerCase();
    }

    public AdminLevel getLevel() {
        return level;
    }

    public void setLevel(AdminLevel level) {
        this.level = level == null ? AdminLevel.ADMIN : level;
    }

    public boolean isManager() {
        return level == AdminLevel.MANAGER;
    }

    public java.time.LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(java.time.LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
