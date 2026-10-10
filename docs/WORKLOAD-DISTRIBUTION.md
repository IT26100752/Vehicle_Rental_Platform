# Workload Distribution — Vehicle Rental Service Management System

Six components, one per team member (same structure as the intro-session video,
mapped onto the real files of this repository). **Each member commits only
inside their own rows** — that is both the conflict-avoidance rule and the
evidence of individual contribution.

---

## Member 1 — User Management (foundation layer)

**Responsibility:** accounts, authentication, profile; owns the file-handling
core every other module reuses.

| Layer | Files |
|---|---|
| model | `model/User.java` (abstract), `model/CustomerUser.java`, `model/AdminUser.java`, `model/Identifiable.java` |
| util | `util/PasswordUtil.java`, `util/SessionUtil.java` |
| repository | `repository/JsonFileStore.java`, `repository/AbstractFileRepository.java`, `repository/UserRepository.java` |
| service | `service/UserService.java`, `service/CrudService.java`, `service/AbstractCrudService.java` |
| controller | `controller/AuthController.java`, `controller/UserController.java` |
| UI | `templates/auth/login.html`, `auth/register.html`, `user/users.html`, `user/edit.html`, `user/profile.html` |

**CRUD:** Create = register customer · Read = search/list users · Update = edit
user / own profile / change password · Delete = remove user.
**OOP:** encapsulation (private fields), inheritance (`AdminUser`/`CustomerUser
extends User`), polymorphism (`describeRole()`), information hiding (SHA-256
password hash, session helper).

---

## Member 2 — Vehicle Management

**Responsibility:** manage the rental fleet (fields from the video: vehicleNumber,
brand, model, type, year, pricePerDay, fuelType, transmission, seats,
availability, image).

| Layer | Files |
|---|---|
| model | `model/Vehicle.java` (abstract), `Car.java`, `Van.java`, `Bike.java`, `Truck.java`, `VehicleType/FuelType/Transmission/VehicleStatus` enums |
| repository | `repository/VehicleRepository.java` |
| service | `service/VehicleService.java` |
| controller | `controller/VehicleController.java` |
| UI | `templates/vehicle/vehicles.html`, `vehicle/details.html`, `vehicle/form.html` (add + edit) |

**CRUD:** Create = add vehicle (type select = factory for the subclass) ·
Read = list/search/details · Update = edit · Delete = remove.
**OOP:** inheritance (4 subclasses), polymorphism (`capacityInfo()`,
`getType()`), abstraction (fleet code talks only to `Vehicle`).

---

## Member 3 — Booking / Rental Management (business core)

**Responsibility:** reservations, pricing, returns, fines
(fields: pickupDate, returnDate, pickupLocation, returnLocation, totalAmount, status).

| Layer | Files |
|---|---|
| model | `model/Booking.java`, `model/BookingStatus.java` |
| repository | `repository/BookingRepository.java` |
| service | `service/BookingService.java`, `service/pricing/*` (PricingStrategy, Standard, Premium, DiscountPolicy, LongTerm, Weekend), `service/fine/*` (FinePolicy, Standard, Premium) |
| controller | `controller/BookingController.java` |
| UI | `templates/booking/form.html` (+JS live estimate), `confirmation.html`, `list.html`, `details.html` (return/cancel) |

**Flow (as in the video):** search vehicle → select → choose dates → JS preview
of days × price → create booking → confirmation; later return → fine → completed.
**OOP:** runtime polymorphism (strategy per customer type), abstraction
(template method in `DiscountPolicy`), encapsulation of booking state changes.

---

## Member 4 — Payment Management

**Responsibility:** simulated payments (CARD / CASH / ONLINE), history, status updates
(fields: amount, paymentMethod, paymentDate, paymentStatus, transactionReference).

| Layer | Files |
|---|---|
| model | `model/Payment.java`, `PaymentMethod.java`, `PaymentStatus.java` |
| repository | `repository/PaymentRepository.java` |
| service | `service/PaymentService.java` |
| controller | `controller/PaymentController.java` |
| UI | `templates/payment/form.html`, `payment/history.html`, `payment/details.html` |

**CRUD:** Create = payment for a completed booking (amount = total + fine,
auto `TXN-…` reference) · Read = my history / all history + revenue ·
Update = change status (PENDING→COMPLETED/FAILED/REFUNDED) ·
Delete = (optional exercise; history is usually kept — justify this choice in the report).
**OOP:** encapsulation, enum-based state machine, abstraction via `CrudService`.

---

## Member 5 — Admin Management + security layer

**Responsibility:** dashboard, admin profile, and the guard that protects every page.
**Does not duplicate other classes** — combines existing services (video's advice).

| Layer | Files |
|---|---|
| config | `config/AuthInterceptor.java`, `config/WebConfig.java`, `config/GlobalModelAdvice.java`, `config/GlobalExceptionHandler.java`, `config/DataSeeder.java` |
| service | `service/AdminService.java` (+ `DashboardStats` record) |
| controller | `controller/AdminController.java` |
| UI | `templates/admin/dashboard.html` (stat cards + recent bookings + fleet split), `admin/profile.html`, `templates/error.html`, `fragments/layout.html` (navbar) |

**Functions:** stat cards (users / vehicles / bookings / revenue), recent
bookings table, pending-review counter, admin profile update, role-based
routing (admin vs customer), friendly error page.
**OOP:** abstraction (dashboard depends on service interfaces), encapsulation
(record value object), information hiding (auth mechanics inside interceptor).

---

## Member 6 — Review & Feedback Management

**Responsibility:** customer reviews with moderation workflow
(fields: rating, comment, reviewDate, status).

| Layer | Files |
|---|---|
| model | `model/Review.java`, `model/ReviewStatus.java` |
| repository | `repository/ReviewRepository.java` |
| service | `service/ReviewService.java` |
| controller | `controller/ReviewController.java` |
| UI | `templates/review/reviews.html`, `review/form.html` (add + edit, star picker JS), `review/moderation.html`; review block inside `vehicle/details.html` |

**CRUD:** Create = submit review (PENDING) · Read = public wall / per vehicle /
my reviews · Update = edit own (goes back to PENDING) · Delete = own review.
Plus admin moderation: approve / reject.
**OOP:** encapsulation, enum state workflow, polymorphism of status rendering
(`badge()`/`label()` helpers).

---

## Shared conventions (agree on day 1)

* Package root `lk.ijse.se1020.vrs` — one package per layer.
* Every entity implements `Identifiable`; every repository extends
  `AbstractFileRepository`; every service extends `AbstractCrudService`.
* Business rule violations throw `IllegalStateException` → friendly error page.
* One data file per entity in `data/`, written only by `JsonFileStore`.
* Branch name `feature/<member>-<module>`; commit prefix = module name.
