package com.vehiclerental.user.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.CustomerTier;
import com.vehiclerental.user.entity.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.Optional;

/** Stores customers in data/customers.txt */
@Repository
public class UserRepository extends FileRepository<Customer> {

    public UserRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "customers.txt"));
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String wanted = email.trim().toLowerCase();
        return findFirst(c -> wanted.equals(c.getEmail()));
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|fullName|email|phone|passwordHash|address|nic|sex|age|licenceId|status|emailVerified|tier";
    }

    @Override
    protected String[] toFields(Customer c) {
        return new String[]{c.getFullName(), c.getEmail(), c.getPhone(), c.getPasswordHash(),
                text(c.getAddress()), text(c.getNic()), text(c.getSex()), text(c.getAge()),
                text(c.getLicenceId()), c.getStatus().name(), String.valueOf(c.isEmailVerified()), c.getTier().name()};
    }

    @Override
    protected Customer fromFields(String[] f) {
        Customer c = new Customer();
        c.setFullName(str(f, 0));
        c.setEmail(str(f, 1));
        c.setPhone(str(f, 2));
        c.setPasswordHash(str(f, 3));
        c.setAddress(strOrNull(f, 4));
        c.setNic(strOrNull(f, 5));
        c.setSex(strOrNull(f, 6));
        c.setAge(integer(f, 7));
        c.setLicenceId(strOrNull(f, 8));
        c.setStatus(enumOf(UserStatus.class, f, 9, UserStatus.ACTIVE));
        c.setEmailVerified(str(f, 10).isEmpty() || bool(f, 10));   // old rows have no column: already verified
        c.setTier(enumOf(CustomerTier.class, f, 11, CustomerTier.STANDARD));
        return c;
    }
}
