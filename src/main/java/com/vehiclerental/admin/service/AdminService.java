package com.vehiclerental.admin.service;

import com.vehiclerental.admin.entity.Admin;
import com.vehiclerental.admin.entity.AdminLevel;
import com.vehiclerental.admin.repository.AdminRepository;
import com.vehiclerental.exception.BusinessRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Administrator accounts: sign-in, and (for the manager admin only) adding, removing and resetting other admins.
 * The "who may do this" rule lives here so that no controller can forget it.
 */
@Service
public class AdminService {

    private final AdminRepository repository;
    private final PasswordEncoder encoder;
    private final ActivityService activity;

    public AdminService(AdminRepository repository, PasswordEncoder encoder, ActivityService activity) {
        this.repository = repository;
        this.encoder = encoder;
        this.activity = activity;
    }

    public Admin authenticate(String username, String rawPassword) {
        Admin admin = repository.findByUsername(username)
                .filter(a -> encoder.matches(rawPassword, a.getPasswordHash()))
                .orElseThrow(() -> new BusinessRuleException("Invalid admin username or password"));
        admin.setLastLoginAt(LocalDateTime.now());
        repository.update(admin);
        activity.log(admin.getFullName(), "Signed in", admin.getLevel().getLabel() + " @" + admin.getUsername());
        return admin;
    }

    public long count() {
        return repository.count();
    }

    /** Manager first, then by name. */
    public List<Admin> findAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing((Admin a) -> a.getLevel().ordinal()).thenComparing(Admin::getUsername))
                .toList();
    }

    public Admin findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessRuleException("That administrator no longer exists"));
    }

    public boolean usernameTaken(String username) {
        return repository.findByUsername(username).isPresent();
    }

    /** Used by the start-up seeder and by {@link #addAdmin}; no permission check. */
    public Admin create(String username, String fullName, String email, String rawPassword, AdminLevel level) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setLevel(level);
        admin.setPasswordHash(encoder.encode(rawPassword));
        return repository.create(admin);
    }

    public Admin create(String username, String fullName, String email, String rawPassword) {
        return create(username, fullName, email, rawPassword, AdminLevel.ADMIN);
    }

    /** Only a manager admin can add administrators. */
    public Admin addAdmin(Admin actor, String username, String fullName, String email, String rawPassword) {
        requireManager(actor);
        String user = username == null ? "" : username.trim().toLowerCase();
        if (!user.matches("[a-z0-9._-]{3,20}")) {
            throw new BusinessRuleException("Username must be 3 - 20 letters, digits, dot, dash or underscore");
        }
        if (fullName == null || fullName.trim().length() < 2) {
            throw new BusinessRuleException("Enter the full name of the new admin");
        }
        checkPassword(rawPassword);
        if (usernameTaken(user)) {
            throw new BusinessRuleException("The username @" + user + " is already used");
        }
        Admin created = create(user, fullName, email == null || email.isBlank() ? null : email, rawPassword, AdminLevel.ADMIN);
        activity.log(actor.getFullName(), "Added admin", created.getFullName() + " (@" + created.getUsername() + ")");
        return created;
    }

    /** Only a manager admin can delete administrators, never themselves and never another manager. */
    public void deleteAdmin(Admin actor, Long id) {
        requireManager(actor);
        Admin target = findById(id);
        if (target.getId().equals(actor.getId())) {
            throw new BusinessRuleException("You cannot delete your own account");
        }
        if (target.isManager()) {
            throw new BusinessRuleException("A manager admin account cannot be deleted");
        }
        repository.deleteById(id);
        activity.log(actor.getFullName(), "Deleted admin", target.getFullName() + " (@" + target.getUsername() + ")");
    }

    /** Only a manager admin can set a new password for another admin. */
    public void resetPassword(Admin actor, Long id, String newPassword) {
        requireManager(actor);
        checkPassword(newPassword);
        Admin target = findById(id);
        target.setPasswordHash(encoder.encode(newPassword));
        repository.update(target);
        activity.log(actor.getFullName(), "Reset password", target.getFullName() + " (@" + target.getUsername() + ")");
    }

    /** Any admin can change their own password. */
    public void changeOwnPassword(Long id, String current, String newPassword) {
        Admin admin = findById(id);
        if (!encoder.matches(current == null ? "" : current, admin.getPasswordHash())) {
            throw new BusinessRuleException("Your current password is not correct");
        }
        checkPassword(newPassword);
        admin.setPasswordHash(encoder.encode(newPassword));
        repository.update(admin);
        activity.log(admin.getFullName(), "Changed own password", "@" + admin.getUsername());
    }

    private void requireManager(Admin actor) {
        if (actor == null || !actor.isManager()) {
            throw new BusinessRuleException("Only the manager admin can do this");
        }
    }

    private void checkPassword(String raw) {
        if (raw == null || raw.length() < 8) {
            throw new BusinessRuleException("The password must be at least 8 characters");
        }
    }
}
