package com.vehiclerental.admin.repository;

import com.vehiclerental.admin.entity.Admin;
import com.vehiclerental.admin.entity.AdminLevel;
import com.vehiclerental.common.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.Optional;

/** Stores administrators in data/admins.txt */
@Repository
public class AdminRepository extends FileRepository<Admin> {

    public AdminRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "admins.txt"));
    }

    public Optional<Admin> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        String wanted = username.trim().toLowerCase();
        return findFirst(a -> wanted.equals(a.getUsername()));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|username|fullName|email|phone|passwordHash|level|lastLoginAt";
    }

    @Override
    protected String[] toFields(Admin a) {
        return new String[]{a.getUsername(), a.getFullName(), text(a.getEmail()), text(a.getPhone()), a.getPasswordHash(),
                a.getLevel().name(), text(a.getLastLoginAt())};
    }

    @Override
    protected Admin fromFields(String[] f) {
        Admin a = new Admin();
        a.setUsername(str(f, 0));
        a.setFullName(str(f, 1));
        a.setEmail(strOrNull(f, 2));
        a.setPhone(strOrNull(f, 3));
        a.setPasswordHash(str(f, 4));
        a.setLevel(enumOf(AdminLevel.class, f, 5, AdminLevel.ADMIN));   // old rows without a level are ordinary admins
        a.setLastLoginAt(dateTime(f, 6));
        return a;
    }
}
