package user.repository;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.common.repository.AbstractFileRepository;
import com.vehiclerental.common.repository.DataStore;
import com.vehiclerental.user.entity.CustomerUser;
import com.vehiclerental.user.entity.User;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Member 1's repository - everything about the users storage.
 */
@Repository
public class UserRepository extends AbstractFileRepository<User> {

    public UserRepository(DataStore<User> store) {
        super(store);
    }

    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public boolean usernameTaken(String username) {
        return findByUsername(username).isPresent();
    }

    public List<User> findAllCustomers() {
        return findAll().stream()
                .filter(u -> u instanceof CustomerUser)
                .toList();
    }

    public List<User> findAllAdmins() {
        return findAll().stream()
                .filter(u -> u instanceof AdminUser)
                .toList();
    }

    /** Simple search over name / username / email (the READ operation). */
    public List<User> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String q = keyword.toLowerCase();
        return findAll().stream()
                .filter(u -> u.getFullName().toLowerCase().contains(q)
                        || u.getUsername().toLowerCase().contains(q)
                        || u.getEmail().toLowerCase().contains(q))
                .toList();
    }
}
