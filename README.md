# Vehicle Rental Management System

Team YISI · Group G2026.MTR02 · SE1020 Object Oriented Programming

One merged system (the VehicleRentalService base plus the best of Project_OOP). Customers register with **email verification**, pick a car, van, bike or truck (real photos), choose **self drive or with a driver**, choose pickup and drop-off locations from drop-downs, pay by **card, mobile wallet or bank transfer** (animated verification tick), get a **PDF receipt** by email, review (bad reviews are accepted too) and file **complaints**. Admins manage everything, verify bank transfers, assign drivers and receive **notifications** for everything that needs action.

The layout fills the whole window and the sun / moon button in the top bar switches between **dark and light mode** (your choice is remembered in the browser).

The home page has a **Sri Lanka map** with a pin for every rental location. Admins can set exact map coordinates under Admin, Locations (optional; if left blank the pin is placed from the city name).

## Requirements
- Java 17+ and Maven 3.8+

## Run
```
mvn spring-boot:run
```
Open http://localhost:8080

### Sign-in details (for the team and the viva)
These are not shown anywhere in the app. Use the **Administrator** tab on the sign-in page for the admin account.

| Role | Login | Password |
|------|-------|----------|
| Admin | `admin` | `Admin@123` |
| Customer (standard) | `kasun@rental.lk` | `Demo@123` |
| Customer (premium) | `premium@rental.lk` | `Premium@123` |

Sample data (10 vehicles, 5 locations, 3 drivers, bookings in every state, a low review, a complaint) is created the first time the app starts. Delete the `data/` folder to reset.

### Test payments (simulated)
| Method | Approved | Declined / other |
|--------|----------|------------------|
| Card | `4242 4242 4242 4242`, any future expiry, any CVV | `4000 0000 0000 0002` is always declined |
| Wallet | any `07XXXXXXXX` number and any 4-6 digit PIN | PIN `0000` = not enough balance |
| Bank transfer | fill bank, slip reference, date: goes to **Awaiting verification**, the admin approves or rejects it under Admin, Payments | |

## Feature map (what was asked for)
| Feature | Where |
|---------|-------|
| Real vehicle images | `static/images/vehicles` and `static/images/photos`, picked per vehicle in the admin form |
| Email verification | 6-digit code after registering: `/verify-email` (see `docs/EMAIL-VERIFICATION.md`) |
| Complaints tab | customer: `/complaints`; admin: Admin, Complaints (tabs by status, replies, priority) |
| With / without driver | booking form toggle, driver fee per day, auto-assigned driver, Admin, Drivers |
| Pickup / drop-off drop-downs | booking form, locations managed in Admin, Locations; different drop-off adds a one-way fee |
| Driver notification | driver gets an email with a trip link and confirms it; customer and admins get bell notifications |
| Payment methods | card, wallet, bank transfer (admin approval), cash at the counter (admin) |
| Verification tick animation | "Verifying your payment" overlay: spinner, then tick (paid), clock (pending) or cross (failed) |
| Receipt generator and download | `/payments/{id}/receipt` on screen, `/payments/{id}/receipt.pdf` download, PDF attached to the email |
| Email handling | every email is logged (Admin, Email log) with retry; real SMTP or saved to `data/outbox/` (see `docs/EMAIL-GUIDE.md`) |
| Bad reviews accepted | 1 to 5 stars all published; 1-2 stars alert the admin, who can reply publicly |
| Admin notifications | bell with live count, list page, plus an alert email, for new bookings, transfers to verify, complaints, bad reviews, drivers |
| Animations and transitions | scroll reveal, count-up, page transitions, car-on-road loader, segmented switches, payment tick |
| Class diagrams | `docs/class-diagram.md` and pictures in `docs/diagrams/` |
| Step by step guide | `docs/HOW-TO.md` |

## Pricing and rules
- Price = rental (days x price per day) minus discounts, plus driver fee, plus one-way fee. The booking page shows each line live.
- Premium customers: 10% off rentals of 3+ days and a softer late fee. Everyone: 5% off 7+ days, 2% off Saturday pickups.
- Driver fee `app.driver.fee-per-day` (default 3500) and one-way fee `app.booking.one-way-fee` (default 2500) are in `application.properties`. Bikes are self drive only.
- Booking status: `PENDING_PAYMENT -> CONFIRMED -> ACTIVE -> COMPLETED`, or `-> FINE_DUE -> COMPLETED`, `CANCELLED`; a bank transfer adds `AWAITING_VERIFICATION` (see diagram 5).
- Payment hold: 15 minutes. Cancelled paid bookings are refunded and the driver is released.
- Late fine per tier (standard `app.late-fee.percent-per-day`, default 10% of the rental per extra day; premium: first late day free, then 5%).
- Admin verifies the original NIC (and licence for self drive) at pickup. `app.allow-early-handover=true` (default) lets the admin activate and return on any date; set it to `false` to allow handover only from the pickup date.
- A new customer's 6-digit code is sent by email. With email off (the default) it is saved in `data/outbox/` and printed in the console where the app runs; see `docs/EMAIL-GUIDE.md` for real email.

## Data storage (file handling)
Plain text files in `data/`, one per entity, `|` separated, atomic writes, cached reads. `database/database.sql` and `database/sample-data.sql` are the matching MySQL schema and sample data (documentation of the same model; the running app uses the files). `docs/DATABASE-GUIDE.md` explains how to move to MySQL.

## Project structure
Package-by-feature under `com.vehiclerental`: `user`, `vehicle`, `location`, `driver`, `booking` (with `pricing`), `payment` (with `processor` and `receipt`), `review`, `complaint`, `notification`, `admin`, plus `common`, `config`, `exception`. Templates in `src/main/resources/templates`, CSS/JS/images in `static`.

## Suggested team split (GitHub contribution)
| Member | Package / area |
|--------|----------------|
| 1 | `user` (register, email verification, login, profile) |
| 2 | `vehicle` and `location` (four types, images, admin CRUD) |
| 3 | `booking` and `booking/pricing` (availability, lifecycle, returns, fines) |
| 4 | `payment` (processors, receipt PDF, transfer verification) |
| 5 | `driver`, `review`, `complaint` |
| 6 | `notification`, `admin` (dashboard), `database/`, docs |

Commit each member's work under their own account with small, regular commits.

## Known limitations
- No CSRF protection (session login, BCrypt passwords); add Spring Security for production.
- Card, wallet and bank payments are simulated; no real gateway.
- The SQL database is not connected at runtime (see above).
- The driver trip link is protected only by its long random token.
