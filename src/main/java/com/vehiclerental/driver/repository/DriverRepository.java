package com.vehiclerental.driver.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.driver.entity.Driver;
import com.vehiclerental.driver.entity.DriverStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

/** Stores drivers in data/drivers.txt */
@Repository
public class DriverRepository extends FileRepository<Driver> {

    public DriverRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "drivers.txt"));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|fullName|phone|email|licenceNo|languages|status";
    }

    @Override
    protected String[] toFields(Driver d) {
        return new String[]{text(d.getFullName()), text(d.getPhone()), text(d.getEmail()), text(d.getLicenceNo()),
                text(d.getLanguages()), d.getStatus().name()};
    }

    @Override
    protected Driver fromFields(String[] f) {
        Driver d = new Driver();
        d.setFullName(str(f, 0));
        d.setPhone(strOrNull(f, 1));
        d.setEmail(strOrNull(f, 2));
        d.setLicenceNo(strOrNull(f, 3));
        d.setLanguages(strOrNull(f, 4));
        d.setStatus(enumOf(DriverStatus.class, f, 5, DriverStatus.AVAILABLE));
        return d;
    }
}
