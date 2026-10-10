# 🚗 Vehicle Rental Service Management System — SE1020 Project Guide

> Your companion to the **OOP Web Project introduction session** video.
> The video explains *what* to build and how to split it across 6 team members.
> This guide explains *how to actually build it*, using the complete reference
> implementation in this folder (`vehicle-rental-system/`).

---

## Part 0 — What you have in this workspace

| Path | What it is |
|---|---|
| `vehicle-rental-system/` | **Fully working Spring Boot app** (all 6 modules, CRUD + file handling + MySQL + UI) |
| `vehicle-rental-system/docs/GUIDE.md` | This guide |
| `vehicle-rental-system/docs/MYSQL-SETUP.md` | Install MySQL, run `schema.sql`, switch storage modes |
| `vehicle-rental-system/docs/GITHUB-SETUP.md` | Team repository workflow |
| `vehicle-rental-system/docs/WORKLOAD-DISTRIBUTION.md` | The 6-member split (same structure as the video) mapped onto real files |
| `vehicle-rental-system/docs/CLASS-DIAGRAM.puml` | Class diagram source (paste into plantuml.com or the IntelliJ PlantUML plugin) |
| `vehicle-rental-system/README.md` | Quick-start for the whole team |
| `vehicle-rental-system/src/main/resources/db/schema.sql` | The MySQL schema (5 tables, run once per PC) |
| `vehicle-rental-system/data/*.json` | The file-mode storage — plain text files created on first run |

**How to use it:** don't submit this code as-is. Use it as the map: each member
re-builds *their own module* in the team repository (that is what the GitHub
commit-history marks require), and uses this implementation to check behaviour,
steal patterns, and debug. Section 4 gives each member a build order.

---

## Part 1 — Read the marking guide first (work backwards from the marks)

| Criterion | Marks | Where it is won in this codebase |
|---|---|---|
| CRUD operations (min 3 per member) | **30** | Every module: `*Controller` (endpoints) + `*Service` (rules) + `AbstractFileRepository` (persistence). Users/Vehicles/Bookings/Payments/Reviews all have C, R, U, D. |
| OOP concepts | **20** | `User`/`AdminUser`/`CustomerUser` + `Vehicle`/`Car`/`Van`/`Bike`/`Truck` (inheritance, encapsulation), `PricingStrategy`/`FinePolicy` (polymorphism), `AbstractCrudService`, `DiscountPolicy`, `CrudService` (abstraction), `PasswordUtil` + private fields (information hiding) |
| File handling | **10** | `repository/JsonFileStore.java` — *all* reads/writes go through it (`data/*.json`) |
| UI design | **10** | `resources/templates/**` — Bootstrap 5 + Thymeleaf + a little JavaScript (live price estimate on the booking form, exactly like the video suggests) |
| Individual contribution & GitHub history | **10** | See Part 6 — one branch per member, commits in your own name |
| Presentation & viva | **10** | Part 8 question list; know *your* module line by line |
| Documentation (class diagram + report) | **10** | `docs/CLASS-DIAGRAM.puml` + report outline in Part 7 |

Rule of thumb: **the viva focuses on backend Java + file handling**, so every
member must be able to open their service class and explain every line.

---

## Part 2 — One-time environment setup

1. **JDK 17** (IntelliJ: *File → Project Structure → SDK*). Spring Boot 3 needs 17+.
2. **IntelliJ IDEA** (Community is fine) with the bundled **Maven**.
3. **Git** + a GitHub account each.
4. Open the folder `vehicle-rental-system` with *File → Open* (IntelliJ detects `pom.xml` → "Load Maven project").
5. Run `lk.ijse.se1020.vrs.VehicleRentalApplication` (green arrow) **or**
   `mvn spring-boot:run` from the project root.
6. Browse to `http://localhost:8080`.

**Demo logins (created by `DataSeeder` on first run):**

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin (dashboard, moderation, user & vehicle management) |
| `nimal` | `nimal123` | Premium customer (gets pricing discounts) |
| `kasun` | `kasun123` | Regular customer |

First run creates `data/users.json`, `vehicles.json`, `bookings.json`,
`payments.json`, `reviews.json` with sample rows — **these are the sample data
files your deliverable list asks for**. Delete the `data/` folder to reset.

> ⚠️ Run the app **from the project root** so the relative path `data/`
> (set in `application.properties → app.data-dir`) points inside the project.

---

## Part 3 — How the application is built (the architecture you must explain)

```
 Browser (Bootstrap/Thymeleaf HTML + a little JS)
    │  HTTP GET/POST
    ▼
 Controller   (spring web)      ← reads form params, calls services, picks a view
    ▼
 Service      (business rules)  ← validation, pricing, fines, status changes
    ▼
 Repository   (queries)         ← findById / search / findByUsername ...
    ▼
 JsonFileStore (FILE HANDLING)  ← reads & writes data/*.json with Jackson
```

**Life of one request — "create booking":**

1. `booking/form.html` posts to `/bookings/create`.
2. `AuthInterceptor` (config/) checks the session: not logged in → redirect `/login`.
3. `BookingController.create(...)` reads the form fields and calls
   `BookingService.createBooking(...)`.
4. `BookingService` validates dates, asks `VehicleService` whether the vehicle
   is `AVAILABLE`, **chooses a `PricingStrategy` at runtime** (premium vs
   standard customer → polymorphism), applies `DiscountPolicy` list, saves via
   `BookingRepository` → `JsonFileStore.writeAll(...)` → `data/bookings.json`,
   then flips the vehicle to `RENTED`.
5. Controller redirects to `/bookings/{id}/confirmation`; Thymeleaf renders it.

**Why both JSON files *and* MySQL?** The project brief allows
"file read/write (read and write to a notepad)" **or** "a proper database
connection such as MySQL". We keep **both** and choose at start-up with
`app.storage` (`mysql` = default, `file` = JSON fallback):

- `repository/DataStore.java` — the storage abstraction (`readAll/writeAll/
  isEmpty/describe`);
- `repository/JsonFileStore.java` — implementation #1 (Jackson + text files);
- `repository/mysql/MySqlStore.java` + `*Mapping.java` — implementation #2
  (JdbcTemplate + the 5 tables from `db/schema.sql`);
- `config/StorageConfig.java` — picks one per entity and injects it into the
  repositories. Services and controllers are 100% identical in both modes.

Viva line: *"This is the Strategy pattern applied to persistence — the file
handling marks come from `JsonFileStore`, the database marks from `MySqlStore`,
and the abstraction lets us swap storage without touching any business logic."*
MySQL install steps: **`docs/MYSQL-SETUP.md`**. JSON *is* a notepad file you
can open in Notepad — and Jackson does the parsing so you don't fight with
`split(",")`.

---

## Part 4 — The six modules (the video's split) and each member's build order

Full file-by-file ownership table: **`docs/WORKLOAD-DISTRIBUTION.md`**.
Summary + recommended build order per member:

### Member 1 — User Management (+ login/register)
Files: `model/User.java`, `CustomerUser.java`, `AdminUser.java`, `repository/UserRepository.java`,
`service/UserService.java`, `controller/AuthController.java`, `controller/UserController.java`,
`templates/auth/*`, `templates/user/*`, `util/PasswordUtil.java`, `util/SessionUtil.java`.
Build order: ① `User` hierarchy with hashed password → ② `JsonFileStore`+`AbstractFileRepository`
(you own the file-handling core the whole team depends on!) → ③ register + login →
④ user list/search/edit/delete for admin → ⑤ profile + change password.
CRUD: create=register, read=search/list, update=edit/profile, delete=remove user.

### Member 2 — Vehicle Management
Files: `model/Vehicle.java` + `Car/Van/Bike/Truck`, `VehicleRepository`, `VehicleService`,
`VehicleController`, `templates/vehicle/*`.
Build order: ① abstract `Vehicle` + 4 subclasses → ② list + search page → ③ add form
(the `type` select is your factory: it decides which subclass to instantiate) →
④ edit → ⑤ delete. Show `capacityInfo()` on the details page — that is your
polymorphism demo (one call, four different answers).

### Member 3 — Booking / Rental Management (the business core)
Files: `model/Booking.java`, `BookingRepository`, `service/BookingService.java`,
`service/pricing/*`, `service/fine/*`, `BookingController`, `templates/booking/*`.
Build order: ① `Booking` entity + `getRentalDays()` → ② create booking with
availability check + vehicle status flip → ③ `PricingStrategy` interface with
`StandardPricing`/`PremiumPricing` (runtime polymorphism) → ④ return flow with
`FinePolicy` → ⑤ cancel flow. Add the JavaScript days × price preview on the form
(the video explicitly suggests it).

### Member 4 — Payment Management
Files: `model/Payment.java` + enums, `PaymentRepository`, `PaymentService`,
`PaymentController`, `templates/payment/*`.
Build order: ① entity + `TXN-…` reference generator → ② create PENDING payment from a
completed booking (amount = total + fine) → ③ history pages (mine / all + revenue) →
④ status update (PENDING→COMPLETED/FAILED/REFUNDED). Payments are **simulated**
(CARD / CASH / ONLINE) exactly as the intro session allows.

### Member 5 — Admin Management
Files: `service/AdminService.java`, `controller/AdminController.java`,
`templates/admin/*`, plus `config/AuthInterceptor.java` + `WebConfig.java` + `GlobalModelAdvice.java`.
Key idea from the video: **admin does NOT duplicate other classes** — the dashboard
combines `UserService.count()`, `VehicleService.fleetSummaryByType()`,
`BookingService.recent(5)`, `PaymentService.revenue()`. You also own the
login-guard interceptor (great viva topic) and the admin profile page.

### Member 6 — Review & Feedback Management
Files: `model/Review.java`, `ReviewRepository`, `ReviewService`, `ReviewController`,
`templates/review/*`.
Build order: ① entity + star helper → ② submit (status PENDING) → ③ public wall +
per-vehicle reviews on the vehicle page → ④ edit/delete own review → ⑤ admin
moderation queue (approve/reject). Average rating per vehicle is a nice read-operation extra.

---

## Part 5 — OOP concept map (memorise the right-hand column for the viva)

| Concept | Where | One-line viva answer |
|---|---|---|
| Encapsulation | `User`, `Vehicle`, `Booking`… | "All fields are private; state is only reachable through getters/setters, so no class can corrupt another class's data." |
| Inheritance | `AdminUser/CustomerUser extends User`; `Car/Van/Bike/Truck extends Vehicle` | "Shared state (id, name, price…) lives once in the base class; subclasses add only what is unique." |
| Polymorphism | `describeRole()`, `capacityInfo()` overrides; `PricingStrategy`/`FinePolicy` chosen at runtime | "The same method call behaves differently depending on the actual object; `BookingService` picks premium vs standard pricing at runtime." |
| Abstraction | `CrudService` interface, `AbstractCrudService`, `AbstractFileRepository`, `DiscountPolicy` template method | "Controllers depend on the interface/abstract class, not on implementations; the fixed algorithm lives in the abstract class (`apply`), the varying part stays abstract." |
| Information hiding | `PasswordUtil` (SHA-256 hash), private `passwordHash`, `AuthInterceptor` hiding auth mechanics, `JsonFileStore` hiding I/O | "Other parts of the system never see plain passwords or file streams — only safe operations." |

---

## Part 6 — GitHub playbook (10 marks + report evidence)

1. One organisation repo, e.g. `se1020-teamXX-vehicle-rental`.
2. Protect `main`; each member works on `feature/<name>-<module>`
   (e.g. `feature/nimal-vehicles`) and opens a **pull request** per feature.
3. Ownership rule = the table in `WORKLOAD-DISTRIBUTION.md`: you only commit
   inside your own files ⇒ **zero merge conflicts**.
4. Commit messages: `vehicles: add search by type filter`, `bookings: late-fine policy`.
   Small commits, often (the history *is* the evidence of individual work).
5. Screenshot for the report: `git log --graph --oneline --all` + the GitHub
   contributors/commits page.
6. At the viva, open **your** commits and walk through one end to end.

---

## Part 7 — Documentation deliverables

* **Class diagram**: open `docs/CLASS-DIAGRAM.puml`, export PNG/SVG, paste into
  the report. Regenerate after you change classes.
* **Report outline**: 1 Introduction · 2 Objectives · 3 Technologies ·
  4 Architecture (layer diagram from Part 3) · 5 Module descriptions
  (copy your rows from WORKLOAD-DISTRIBUTION) · 6 OOP concept application
  (Part 5 table, with code snippets) · 7 File handling design · 8 UI screenshots ·
  9 Git commit history (screenshots) · 10 Testing/demo scenarios · 11 Conclusion.

---

## Part 8 — Viva prep (expect these)

1. *Where is your data stored?* → `data/*.json`, read/written only by `JsonFileStore`.
2. *Show me your CRUD.* → open your controller: 4 endpoints, 4 service calls.
3. *Where is inheritance?* → your entity hierarchy; explain `extends`.
4. *Where is polymorphism?* → an overridden method + a runtime-chosen strategy.
5. *Why is `Vehicle` abstract?* → a generic "vehicle" can never be rented; only a car/van/bike/truck with real capacity data can.
6. *How does login work?* → session attribute set in `AuthController`, checked by `AuthInterceptor`.
7. *Why hash passwords?* → file leak ≠ password leak; SHA-256 in `PasswordUtil`.
8. *What happens if two users book the same vehicle?* → status check in `BookingService.createBooking` (mention `synchronized` repositories as your concurrency story).
9. *How is the total calculated?* → days × pricePerDay via `PricingStrategy`, then discount policies, then fine on return.
10. *What would you improve?* → real DB behind the repository interface, validation annotations, unit tests — say it with a smile.

---

## Part 9 — "But the PDF says JSP Servlets…"

The description lists **Spring Boot** in technologies and **JSP servlets** in the
sample. This build uses Spring Boot + Thymeleaf (the modern equivalent).
If your lecturer insists on classic JSP:

| This project | Classic JSP equivalent |
|---|---|
| `*Controller` | `*Servlet` with `doGet/doPost` |
| `templates/*.html` (Thymeleaf) | `webapp/**/*.jsp` with JSTL `<c:forEach>` |
| `SessionUtil` + `AuthInterceptor` | `session.getAttribute(...)` + `Filter` |
| embedded Tomcat | external Tomcat war deployment |

The model/service/repository/file-handling layers stay **identical** — that is
the layering argument, and it is where most marks are.

---

## Part 10 — Troubleshooting

| Symptom | Fix |
|---|---|
| Port 8080 in use | change `server.port` in `application.properties` |
| MySQL: `Communications link failure` / `Access denied` / `Unknown database 'vrs'` | see `docs/MYSQL-SETUP.md` §6 (service not running / wrong password / run `schema.sql`) |
| Want to run without any database | set `app.storage=file` in `application.properties` |
| `data/` files not found | run from project root, or set `app.data-dir` to an absolute path |
| Corrupted json after manual edit | delete that file (seeder recreates only when users.json is empty — delete the whole `data/` folder to reseed) |
| Template changes not showing | `spring.thymeleaf.cache=false` is set; restart anyway if unsure |
| Bootstrap looks broken offline | CDN needs internet; download bootstrap.css into `static/css` if required |

---

## Part 11 — Safe extensions (only after the basics work)

* Unit tests for pricing/fine policies (`spring-boot-starter-test` is already in `pom.xml`).
* `@NotBlank`/`@Min` bean validation on form objects.
* Receipt PDF export, email mock, vehicle image upload to `static/uploads/`.

Good luck — build it module by module, commit as you go, and the marks follow. 🎓
