package com.vehiclerental.admin.service;

import com.vehiclerental.admin.entity.AdminActivity;
import com.vehiclerental.admin.repository.AdminActivityRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Writes and reads the staff activity log. Logging never breaks the action being logged. */
@Service
public class ActivityService {

    private final AdminActivityRepository repository;

    public ActivityService(AdminActivityRepository repository) {
        this.repository = repository;
    }

    public void log(String adminName, String action, String detail) {
        try {
            AdminActivity a = new AdminActivity();
            a.setAdminName(adminName == null || adminName.isBlank() ? "System" : adminName);
            a.setAction(action);
            a.setDetail(detail == null ? "" : detail);
            repository.create(a);
        } catch (RuntimeException ignored) {
            // the log is a convenience, so a write problem must not stop the real work
        }
    }

    /** Newest first; q matches the admin, the action or the detail. */
    public List<AdminActivity> search(String q, int limit) {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(a -> needle.isEmpty()
                        || a.getAdminName().toLowerCase(Locale.ROOT).contains(needle)
                        || a.getAction().toLowerCase(Locale.ROOT).contains(needle)
                        || a.getDetail().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(AdminActivity::getCreatedAt).reversed())
                .limit(limit)
                .toList();
    }

    public long count() {
        return repository.count();
    }
}
