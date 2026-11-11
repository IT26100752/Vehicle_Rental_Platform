package com.vehiclerental.admin.repository;

import com.vehiclerental.admin.entity.AdminActivity;
import com.vehiclerental.common.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

/** Stores the staff activity log in data/admin-activity.txt */
@Repository
public class AdminActivityRepository extends FileRepository<AdminActivity> {

    public AdminActivityRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "admin-activity.txt"));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|adminName|action|detail";
    }

    @Override
    protected String[] toFields(AdminActivity a) {
        return new String[]{text(a.getAdminName()), text(a.getAction()), text(a.getDetail())};
    }

    @Override
    protected AdminActivity fromFields(String[] f) {
        AdminActivity a = new AdminActivity();
        a.setAdminName(str(f, 0));
        a.setAction(str(f, 1));
        a.setDetail(str(f, 2));
        return a;
    }
}
