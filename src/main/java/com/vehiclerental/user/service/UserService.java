package com.vehiclerental.user.service;

import com.vehiclerental.common.Validators;
import com.vehiclerental.exception.BusinessRuleException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.user.dto.PasswordChangeForm;
import com.vehiclerental.user.dto.ProfileForm;
import com.vehiclerental.user.dto.RegisterRequest;
import com.vehiclerental.user.dto.UserEditForm;
import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.UserStatus;
import com.vehiclerental.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** Customer accounts: register, sign in, update, block and delete (CRUD + authentication). */
@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // ------------------------------------------------------------ Create

    public Customer register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("The two passwords do not match");
        }
        Customer existing = repository.findByEmail(request.getEmail()).orElse(null);
        if (existing != null && existing.isEmailVerified()) {
            throw new BusinessRuleException("This email is already registered. Try signing in instead.");
        }
        // An unverified account for the same email is refreshed (new details + password) instead of rejected.
        Customer customer = existing != null ? existing : new Customer();
        customer.setEmailVerified(false);
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setPasswordHash(encoder.encode(request.getPassword()));
        return existing != null ? repository.update(customer) : repository.create(customer);
    }

    /** True when a VERIFIED account already uses this email (an unverified one can be re-registered). */
    public boolean emailTaken(String email) {
        return repository.findByEmail(email).map(Customer::isEmailVerified).orElse(false);
    }

    // -------------------------------------------------------------- Read

    public Customer authenticate(String email, String rawPassword) {
        Customer customer = repository.findByEmail(email)
                .filter(c -> encoder.matches(rawPassword, c.getPasswordHash()))
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));
        if (!customer.isEmailVerified()) {
            throw new EmailNotVerifiedException(customer.getEmail());
        }
        if (!customer.isLoginAllowed()) {
            throw new BusinessRuleException("Your account has been blocked. Please contact the rental company.");
        }
        return customer;
    }

    public Customer findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new BusinessRuleException("We could not find that account."));
    }

    public Customer markEmailVerified(Long id) {
        Customer customer = findById(id);
        customer.setEmailVerified(true);
        return repository.update(customer);
    }

    public Customer findById(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }

    public List<Customer> findAll() {
        List<Customer> all = repository.findAll();
        all.sort(Comparator.comparing(Customer::getId));
        return all;
    }

    /** Search by name, email, phone or NIC, optionally limited to one status. */
    public List<Customer> search(String keyword, UserStatus status) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return findAll().stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> k.isEmpty()
                        || contains(c.getFullName(), k) || contains(c.getEmail(), k)
                        || contains(c.getPhone(), k) || contains(c.getNic(), k))
                .toList();
    }

    public long count() {
        return repository.count();
    }

    // ------------------------------------------------------------ Update

    public Customer updateProfile(Long id, ProfileForm form) {
        Customer customer = findById(id);
        customer.setFullName(form.getFullName());
        customer.setPhone(form.getPhone());
        customer.setAddress(form.getAddress());
        return repository.update(customer);
    }

    public Customer updateByAdmin(Long id, UserEditForm form) {
        Customer customer = findById(id);
        String newEmail = form.getEmail().trim().toLowerCase();
        if (!newEmail.equals(customer.getEmail()) && repository.existsByEmail(newEmail)) {
            throw new BusinessRuleException("Another customer already uses that email");
        }
        customer.setFullName(form.getFullName());
        customer.setEmail(newEmail);
        customer.setPhone(form.getPhone());
        customer.setAddress(form.getAddress());
        customer.setStatus(form.getStatus());
        customer.setTier(form.getTier());
        customer.setEmailVerified(form.isEmailVerified());
        return repository.update(customer);
    }

    public void changePassword(Long id, PasswordChangeForm form) {
        Customer customer = findById(id);
        if (!encoder.matches(form.getCurrentPassword(), customer.getPasswordHash())) {
            throw new BusinessRuleException("Your current password is incorrect");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessRuleException("The new passwords do not match");
        }
        customer.setPasswordHash(encoder.encode(form.getNewPassword()));
        repository.update(customer);
    }

    public Customer setTier(Long id, com.vehiclerental.user.entity.CustomerTier tier) {
        Customer customer = findById(id);
        customer.setTier(tier);
        return repository.update(customer);
    }

    public Customer setBlocked(Long id, boolean blocked) {
        Customer customer = findById(id);
        customer.setStatus(blocked ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        return repository.update(customer);
    }

    /** Remember the last rental details so the next booking form is pre-filled. */
    public void saveRentalDetails(Long id, String nic, String sex, Integer age, String licenceId) {
        Customer customer = findById(id);
        customer.setNic(nic);
        customer.setSex(sex);
        customer.setAge(age);
        customer.setLicenceId(licenceId);
        repository.update(customer);
    }

    // ------------------------------------------------------------ Delete

    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw ResourceNotFoundException.of("Customer", id);
        }
    }

    private static boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase().contains(lowerKeyword);
    }

    /** Kept for services that want to double check a phone number outside of bean validation. */
    public static boolean isValidPhone(String phone) {
        return Validators.isValidPhone(phone);
    }
}
