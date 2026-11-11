
package com.vehiclerental.user.repository;

import com.vehiclerental.user.entity.Customer;
import com.vehiclerental.user.entity.CustomerTier;
import com.vehiclerental.user.entity.UserStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcUserRepository extends UserRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(
            JdbcTemplate jdbc,
            @Value("${app.data-dir:data}") String dataDir) {

        super(dataDir);
        this.jdbc = jdbc;
    }

    // Map MySQL customer rows into Java Customer objects
    private static final RowMapper<Customer> MAPPER = (rs, rowNum) -> {

        Customer c = new Customer();

        c.setId(rs.getLong("id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setPasswordHash(rs.getString("password_hash"));

        c.setAddress(rs.getString("address"));
        c.setNic(rs.getString("nic"));
        c.setSex(rs.getString("sex"));

        Number age = (Number) rs.getObject("age");
        c.setAge(age == null ? null : age.intValue());

        c.setLicenceId(rs.getString("licence_id"));

        c.setStatus(
                UserStatus.valueOf(rs.getString("status"))
        );

        c.setEmailVerified(rs.getBoolean("email_verified"));

        c.setTier(
                CustomerTier.valueOf(rs.getString("tier"))
        );

        Timestamp createdAt = rs.getTimestamp("created_at");

        if (createdAt != null) {
            c.setCreatedAt(createdAt.toLocalDateTime());
        }

        return c;
    };

    // CREATE
    @Override
    public Customer create(Customer c) {

        String sql = """
                INSERT INTO customers
                (full_name, email, phone, password_hash,
                 address, nic, sex, age, licence_id,
                 status, email_verified, tier)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, c.getFullName());
            ps.setString(2, normalizeEmail(c.getEmail()));
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getPasswordHash());

            ps.setString(5, c.getAddress());
            ps.setString(6, c.getNic());
            ps.setString(7, c.getSex());
            ps.setObject(8, c.getAge());
            ps.setString(9, c.getLicenceId());

            ps.setString(10, c.getStatus().name());
            ps.setBoolean(11, c.isEmailVerified());
            ps.setString(12, c.getTier().name());

            return ps;

        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "Could not generate customer ID"
            );
        }

        return findById(generatedId.longValue()).orElseThrow();
    }

    // READ BY ID
    @Override
    public Optional<Customer> findById(Long id) {

        if (id == null) {
            return Optional.empty();
        }

        return jdbc.query(
                "SELECT * FROM customers WHERE id = ?",
                MAPPER,
                id
        ).stream().findFirst();
    }

    // READ ALL
    @Override
    public List<Customer> findAll() {

        return jdbc.query(
                "SELECT * FROM customers ORDER BY id",
                MAPPER
        );
    }

    // UPDATE
    @Override
    public Customer update(Customer c) {

        String sql = """
                UPDATE customers SET
                    full_name = ?,
                    email = ?,
                    phone = ?,
                    password_hash = ?,
                    address = ?,
                    nic = ?,
                    sex = ?,
                    age = ?,
                    licence_id = ?,
                    status = ?,
                    email_verified = ?,
                    tier = ?
                WHERE id = ?
                """;

        int updated = jdbc.update(
                sql,
                c.getFullName(),
                normalizeEmail(c.getEmail()),
                c.getPhone(),
                c.getPasswordHash(),
                c.getAddress(),
                c.getNic(),
                c.getSex(),
                c.getAge(),
                c.getLicenceId(),
                c.getStatus().name(),
                c.isEmailVerified(),
                c.getTier().name(),
                c.getId()
        );

        if (updated == 0) {
            throw new IllegalStateException(
                    "Customer not found with ID: " + c.getId()
            );
        }

        return findById(c.getId()).orElseThrow();
    }

    // DELETE
    @Override
    public boolean deleteById(Long id) {

        return jdbc.update(
                "DELETE FROM customers WHERE id = ?",
                id
        ) > 0;
    }

    // COUNT
    @Override
    public long count() {

        Long result = jdbc.queryForObject(
                "SELECT COUNT(*) FROM customers",
                Long.class
        );

        return result == null ? 0 : result;
    }

    // FIND BY EMAIL
    @Override
    public Optional<Customer> findByEmail(String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return jdbc.query(
                "SELECT * FROM customers WHERE email = ?",
                MAPPER,
                normalizeEmail(email)
        ).stream().findFirst();
    }

    // CHECK EMAIL
    @Override
    public boolean existsByEmail(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM customers WHERE email = ?)",
                Boolean.class,
                normalizeEmail(email)
        );

        return Boolean.TRUE.equals(exists);
    }

    // NORMALIZE EMAIL
    private String normalizeEmail(String email) {

        return email.trim().toLowerCase();
    }
}
