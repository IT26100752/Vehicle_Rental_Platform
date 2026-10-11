package com.vehiclerental.admin.repository;

import com.vehiclerental.user.entity.User;
import com.vehiclerental.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Admin accounts live in the same storage as customers (users table / users.json),
 * so this repository RE-USES UserRepository and only exposes the admin view of it.
 */
@Repository
public class AdminRepository {

    private final UserRepository userRepository;

    public AdminRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAllAdmins() {
        return userRepository.findAllAdmins();
    }

    public long countAdmins() {
        return userRepository.findAllAdmins().size();
    }
}
