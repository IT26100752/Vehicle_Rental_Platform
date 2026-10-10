# MySQL Setup — SE1020 Vehicle Rental System

The project supports **two storage modes** (the brief allows "file read/write
**or** MySQL" — we implemented **both** and you switch with one property):

| Mode | Property | Where records live |
|---|---|---|
| **MySQL** (default) | `app.storage=mysql` | tables `users`, `vehicles`, `bookings`, `payments`, `reviews` in database `vrs` |
| **File** | `app.storage=file` | JSON text files in `data/` (`users.json`, …) |

Nothing else changes: the repositories, services and controllers work against
the `DataStore<T>` interface (Strategy pattern), and `config/StorageConfig.java`
picks the implementation at start-up.

**No MySQL installed yet and just want to run the app now?**
Open `src/main/resources/application.properties` and set:

```properties
app.storage=file
```

The app then starts with zero database requirements (the MySQL connection pool
is lazy and never opens a connection in file mode).

---

## 1. Install MySQL on Windows (one time per team PC)

### Option A — MySQL Installer + MySQL Workbench (recommended)

1. Download **MySQL Installer for Windows** (Community, the small
   `mysql-installer-web-community-8.x.msi`):
   <https://dev.mysql.com/downloads/installer/>
2. Run it → choose **Custom** or **Developer Default**.
   Minimum products to install:
   - **MySQL Server 8.x**
   - **MySQL Workbench** (so you can see your tables — great viva/demo screenshots)
3. During configuration:
   - Type & Networking: **Development Computer**, port **3306** (default) ✔
   - Authentication Method: keep **"Use Strong Password Encryption"** ✔
   - Accounts and Roles: **set a root password — write it down!**
     (Simplest for the project: pick *"Use Legacy Authentication Method" only if
     the connector complains — normally it does not.)
   - Windows Service: keep **"Configure MySQL Server as a Windows Service"**
     and **"Start the MySQL Server at System Startup"** ✔
4. Finish the installer. MySQL now runs in the background on every boot.

### Option B — XAMPP (if you already use it)

1. Install XAMPP → open the XAMPP Control Panel → **Start** the *MySQL* module.
2. XAMPP's root user has an **empty password** — that matches the project's
   default `application.properties`, so you can skip step 2 below entirely.
3. To browse tables use phpMyAdmin: <http://localhost/phpmyadmin>.

---

## 2. Put your password into the app

Open `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vrs?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=        # <-- put YOUR root password here
```

> ⚠️ **Do not commit your personal password** to GitHub if it is sensitive —
> for a class project it is fine; the marker expects to see the connection
> properties. Just keep the password line consistent with what each member
> installed.

---

## 3. Create the tables (one time per PC)

The schema script is in the project: **`src/main/resources/db/schema.sql`**.

**With MySQL Workbench:**
1. Open Workbench → connect to *Local instance MySQL80*.
2. **File → Open SQL Script…** → select `src/main/resources/db/schema.sql`.
3. Press the ⚡ **Execute** button. You should see:
   `CREATE DATABASE vrs` + 5 × `CREATE TABLE` succeed.
4. Refresh the *Schemas* panel → expand **vrs → Tables** — you should see
   `users, vehicles, bookings, payments, reviews`.

**Or from a terminal (Command Prompt):**

```bat
mysql -u root -p < "D:\oop project\vehicle-rental-system\src\main\resources\db\schema.sql"
```

The script is idempotent (`CREATE ... IF NOT EXISTS`) — running it twice is safe.
You do **not** need to insert sample data: on first start with empty tables the
`DataSeeder` fills them (demo logins `admin/admin123`, `nimal/nimal123`,
`kasun/kasun123`).

---

## 4. Run and verify

1. Start the app (`VehicleRentalApplication` ▶ in IntelliJ, or `mvn spring-boot:run`).
2. The console must show:
   ```
   Storage mode = MYSQL (tables in schema 'vrs')
   HikariPool-1 - Start completed.
   Seeding sample data ...
   ```
3. Log in as `admin/admin123`, add a vehicle, make a booking.
4. In Workbench run:
   ```sql
   USE vrs;
   SELECT * FROM vehicles;
   SELECT * FROM bookings;
   ```
   Your records are really in MySQL now. **Screenshot this for the report/viva** —
   it proves the "proper DB connection" criterion.

---

## 5. How the two modes coexist (viva answers)

- **`repository/DataStore.java`** — the storage *abstraction*:
  `readAll() / writeAll() / isEmpty() / describe()`.
- **`repository/JsonFileStore.java`** — implementation #1: Jackson reads/writes
  pretty-printed JSON text files (the *file handling* marks).
- **`repository/mysql/MySqlStore.java`** — implementation #2: Spring
  `JdbcTemplate` runs `SELECT` / `DELETE+INSERT` against a MySQL table
  (the *database* marks).
- **`repository/mysql/*Mapping.java`** — one row-mapper per entity; the
  `type` / `vehicle_type` columns keep inheritance working in SQL exactly like
  the JSON `"type"` property does.
- **`config/StorageConfig.java`** — reads `app.storage` once at start-up and
  injects either `MySqlStore` or `JsonFileStore` into every repository.
  Repositories/services/controllers never know which one they got.

Switching modes never loses code — the *same* CRUD runs on both.

---

## 6. Troubleshooting

| Problem | Fix |
|---|---|
| `Communications link failure` / `Connection refused` on start | MySQL service not running: Windows **Services** → start *MySQL80* (or XAMPP → Start MySQL). |
| `Access denied for user 'root'@'localhost'` | Wrong password in `application.properties` (line `spring.datasource.password`). |
| `Unknown database 'vrs'` | Run `schema.sql` once (section 3). The URL already has `createDatabaseIfNotExist=true`, but the **tables** still need the script. |
| `Table 'vrs.users' doesn't exist` | Same as above — `schema.sql` not executed yet. |
| Port 3306 already in use | Something else (another MySQL/MariaDB) is running; stop it or change the MySQL port and update the URL. |
| I just want it to run for a demo, no DB | Set `app.storage=file` and start — done. |
| Data in MySQL is confusing after tests | `DROP DATABASE vrs;` in Workbench, re-run `schema.sql`, restart the app → fresh seed data. |
