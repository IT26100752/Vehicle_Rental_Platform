package com.vehiclerental.admin.entity;

import com.vehiclerental.user.entity.User;

/**
 * INHERITANCE - an AdminUser IS-A User.
 * POLYMORPHISM - canManageUsers() returns true only for admins.
 */
public class AdminUser extends User {

    private String department;
    private int permissionLevel;   // 1 = junior staff, 3 = super admin

    public AdminUser() {
    }

    public AdminUser(int id, String fullName, String username, String passwordHash,
                     String email, String phone, String department, int permissionLevel) {
        super(id, fullName, username, passwordHash, email, phone);
        this.department = department;
        this.permissionLevel = permissionLevel;
    }

    @Override
    public String describeRole() {
        return "Admin (" + department + ", L" + permissionLevel + ")";
    }

    @Override
    public boolean canManageUsers() {
        return permissionLevel >= 2;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(int permissionLevel) { this.permissionLevel = permissionLevel; }
}
