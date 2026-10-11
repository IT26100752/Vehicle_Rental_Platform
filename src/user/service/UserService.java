package user.service;

import com.vehiclerental.common.service.AbstractCrudService;
import com.vehiclerental.common.util.PasswordUtil;
import com.vehiclerental.user.entity.CustomerUser;
import com.vehiclerental.user.entity.User;
import com.vehiclerental.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * MEMBER 1 - User Management service.
 * CRUD on the users storage + authentication.
 */
@Service
public class UserService extends AbstractCrudService<User> {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    /* ------------------------- CREATE ------------------------------- */
    /**
     * Registers a customer. The plain password never reaches storage:
     * we store only its SHA-256 hash (INFORMATION HIDING).
     *
     * @throws IllegalStateException when the username already exists
     */
    public CustomerUser registerCustomer(String fullName, String username, String plainPassword,
                                         String email, String phone, String nic, String address) {
        if (userRepository.usernameTaken(username)) {
            throw new IllegalStateException("Username '" + username + "' is already taken");
        }
        CustomerUser customer = new CustomerUser(0, fullName, username,
                PasswordUtil.hash(plainPassword), email, phone, nic, address, false);
        return (CustomerUser) save(customer);
    }

    /* ------------------------- READ (login) -------------------------- */
    /**
     * Authenticates a user. Returns empty Optional on bad credentials -
     * we never tell the UI whether the username or the password was wrong
     * (that is information hiding too).
     */
    public Optional<User> authenticate(String username, String plainPassword) {
        return userRepository.findByUsername(username)
                .filter(u -> PasswordUtil.matches(plainPassword, u.getPasswordHash()));
    }

    /* ------------------------- READ (search) ------------------------- */
    public List<User> search(String keyword) {
        return userRepository.search(keyword);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> customers() {
        return userRepository.findAllCustomers();
    }

    /* ------------------------- UPDATE -------------------------------- */
    public boolean changePassword(int userId, String currentPlain, String newPlain) {
        Optional<User> found = findById(userId);
        if (found.isEmpty()) return false;
        User user = found.get();
        if (!PasswordUtil.matches(currentPlain, user.getPasswordHash())) {
            return false;
        }
        user.setPasswordHash(PasswordUtil.hash(newPlain));
        return update(user);
    }
}
