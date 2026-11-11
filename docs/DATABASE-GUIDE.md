# Connecting the MySQL database

Status: the app currently stores data in files (`data/*.txt`). `database/database.sql` is the matching MySQL schema.
Every service talks to the `CrudRepository<T>` interface, so moving to MySQL means writing JDBC versions of the repositories (one per table in `database.sql`) - nothing in services, controllers or templates changes.

## Step 1 - Install MySQL
1. Download MySQL Installer (or XAMPP, which includes MariaDB, also fine) and install **MySQL Server + MySQL Workbench**.
2. Set a root password and remember it. Default port: 3306.

## Step 2 - Create the database
Workbench -> File -> Open SQL Script -> `database/database.sql` -> run (lightning icon). Then open and run `database/sample-data.sql`.
Check: `USE vehicle_rental; SELECT COUNT(*) FROM vehicles;` returns 10.
(Terminal alternative: `mysql -u root -p < database/database.sql` then `mysql -u root -p vehicle_rental < database/sample-data.sql`.)

## Step 3 - Add dependencies (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```
Reload Maven.

## Step 4 - Connection settings (application.properties)
```
spring.datasource.url=jdbc:mysql://localhost:3306/vehicle_rental?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:}
```
Set `DB_PASSWORD` in the IntelliJ run configuration (never commit it to Git).

## Step 5 - Write a JDBC repository (example: customers)
Create `user/repository/JdbcUserRepository.java`. Mark it `@Repository` and `@Primary`, and remove `@Repository` from the file
version (or keep both and let `@Primary` win). It must keep the same public methods as `UserRepository`
(`findByEmail`, `existsByEmail`, plus the CRUD methods) so `UserService` is untouched.

```java
@Repository
public class JdbcUserRepository {          // make it extend/implement the same type your service injects
    private final JdbcTemplate jdbc;
    public JdbcUserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<Customer> MAPPER = (rs, i) -> {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setAddress(rs.getString("address"));
        c.setNic(rs.getString("nic"));
        c.setSex(rs.getString("sex"));
        c.setAge((Integer) rs.getObject("age"));
        c.setLicenceId(rs.getString("licence_id"));
        c.setStatus(UserStatus.valueOf(rs.getString("status")));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return c;
    };

    public Customer create(Customer c) {                                   // C
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO customers (full_name,email,phone,password_hash,address,nic,sex,age,licence_id,status) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getFullName());  ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());     ps.setString(4, c.getPasswordHash());
            ps.setString(5, c.getAddress());   ps.setString(6, c.getNic());
            ps.setString(7, c.getSex());       ps.setObject(8, c.getAge());
            ps.setString(9, c.getLicenceId()); ps.setString(10, c.getStatus().name());
            return ps;
        }, key);
        c.setId(key.getKey().longValue());
        return c;
    }
    public Optional<Customer> findById(Long id) {                          // R
        return jdbc.query("SELECT * FROM customers WHERE id=?", MAPPER, id).stream().findFirst();
    }
    public List<Customer> findAll() { return jdbc.query("SELECT * FROM customers", MAPPER); }
    public Customer update(Customer c) {                                   // U
        jdbc.update("UPDATE customers SET full_name=?,email=?,phone=?,password_hash=?,address=?,nic=?,sex=?,age=?,licence_id=?,status=? WHERE id=?",
            c.getFullName(), c.getEmail(), c.getPhone(), c.getPasswordHash(), c.getAddress(), c.getNic(),
            c.getSex(), c.getAge(), c.getLicenceId(), c.getStatus().name(), c.getId());
        return c;
    }
    public boolean deleteById(Long id) { return jdbc.update("DELETE FROM customers WHERE id=?", id) > 0; }   // D
    public long count() { return jdbc.queryForObject("SELECT COUNT(*) FROM customers", Long.class); }
    public Optional<Customer> findByEmail(String e) {
        return jdbc.query("SELECT * FROM customers WHERE email=?", MAPPER, e.trim().toLowerCase()).stream().findFirst();
    }
}
```
Repeat the same pattern for `admins`, `vehicles` (use the `type` column to build a `Car` or `Van`), `bookings`, `payments`
(`CARD`/`CASH`) and `reviews`. Column names are in `database/database.sql`.

## Step 6 - Switch off the file seeder
`DataSeeder` seeds the files. When using MySQL, the data already comes from `sample-data.sql`, so skip the seeder
(`@ConditionalOnProperty` on it, or remove it).

## Troubleshooting
- `Communications link failure` : MySQL service is not running (Services -> MySQL80 -> Start) or wrong port.
- `Access denied for user 'root'` : wrong password in `DB_PASSWORD`.
- `Unknown database 'vehicle_rental'` : Step 2 was not run.
- `Public Key Retrieval is not allowed` : keep `allowPublicKeyRetrieval=true` in the URL.

## For the report
Explain: schema with primary/foreign keys (ER diagram in `docs/class-diagram.md`), the repository pattern, and why
`CrudRepository<T>` let you swap file storage for MySQL without changing the services.
