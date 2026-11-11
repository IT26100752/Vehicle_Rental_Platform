# How to do every task, step by step

Part A sets the project up. Part B is the customer side, Part C the admin side. Part D is the email setup, Part E a ready-made viva demo script, Part F GitHub.

## Part A - Set up and run

1. Install **JDK 17+** and **Maven** (or open the folder in IntelliJ IDEA, which can do both).
2. Open the project folder `VehicleRentalService` (the one containing `pom.xml`). Let IntelliJ load the Maven project.
3. Run `VehicleRentalApplication` (green play button) or type `mvn spring-boot:run` in a terminal.
4. Open http://localhost:8080. The first start creates the `data/` folder and the sample data.
5. To start from scratch, stop the app, delete the `data/` folder, start again.

Logins are listed in the README (use the **Administrator** tab for `admin`).

## Part B - Customer tasks

### B1. Register and verify the email
1. Click **Register**, fill the form, press **Create account**.
2. You land on the verification page. A 6-digit code was emailed. With email off (the default) open the newest file in `data/outbox/` or look at the console where the app runs; the code is there.
3. Type the code. The tick animation plays and you are taken to sign in. Wrong code: you have 5 tries; **Send a new code** works after the countdown.

### B2. Find a vehicle
1. Open **Vehicles**. Filter by type (Car, Van, Bike, Truck), fuel, price or search by brand.
2. Open a card to see the photo, specs, rating and reviews. Press **Book this vehicle**.

### B3. Book: with or without a driver, pickup and drop-off
1. Choose pickup date, return date and pickup time.
2. Choose **Self drive** or **With a driver** (bikes are self drive only). With a driver you do not need a licence and may add a street address for door-to-door pickup.
3. Pick **Pickup location** and **Drop-off location** from the drop-downs. A different drop-off adds the one-way fee.
4. The price box shows each line live: rental, discounts, driver, one-way fee, total.
5. Fill your name, NIC, sex, age (licence only for self drive), tick the document box and press **Review my booking**, then confirm. You have 15 minutes to pay.

### B4. Pay (card, wallet or bank transfer)
1. On the payment page choose a method card.
2. **Card**: `4242 4242 4242 4242`, any future expiry, any CVV. **Wallet**: choose the wallet, `0771234567`, PIN `1234`. **Bank transfer**: transfer to the shown account, then enter your bank, the slip reference and the date.
3. Press **Pay**. The "Verifying your payment" screen shows steps, then a **tick** (paid), a **clock** (bank transfer waiting for the admin) or a **cross** (declined; the box tells you why). Wrong details are marked under each field.
4. You land on the receipt page.

### B5. Receipt: view, download, email
- On screen: **Payment history** (menu) -> a payment -> receipt page.
- **Download PDF** button on the receipt page, or **PDF** in Payment history.
- The same PDF is attached to the confirmation email (look in `data/outbox/` when email is off).

### B6. Cancel, review, complain
- **My bookings** -> open a booking -> **Cancel** (before the rental starts; paid bookings are refunded).
- After a rental is completed press **Write a review**. Choose any rating from 1 to 5. A 1-2 star review is published too, and the page offers **raise a complaint**.
- **Complaints** (menu) -> **New complaint**: choose category, optionally the booking, write the problem. Follow the thread, reply, or reopen a resolved complaint.
- The **bell** shows messages (payment received, driver assigned, replies).

## Part C - Admin tasks

Sign in at **Login -> Admin tab**. The dashboard's **Needs your attention** row links to: bank transfers to verify, open complaints, bookings without a driver, low reviews to answer.

### C1. Vehicles (add, edit, delete, photos)
**Vehicles -> Add vehicle**. Choose the type; the form shows the right extra fields (doors and boot for car, cargo for van, engine cc for bike, max load for truck). Pick a **Picture** from the list (the real photos); the preview updates. Save.

### C2. Locations and drivers
**Locations** and **Drivers** have an add form on the right and the list on the left. Set a driver to **Off duty** to stop assignments. A driver or location cannot be deleted while it is in use.

### C3. Verify a bank transfer
1. Bell or **Payments -> Awaiting verification** tab.
2. Check your bank account for the amount and slip reference.
3. **Approve**: the booking becomes confirmed, a driver is auto-assigned if needed, the customer gets the receipt PDF.
4. **Reject** (type a reason): the customer is told and can pay again within a fresh 15 minutes.

### C4. Assign or change a driver
A paid with-driver booking gets a free driver automatically (the driver gets an email with a link and presses **I have seen this trip**). To pick one yourself: **Bookings -> open the booking -> Driver card -> choose -> Assign driver**. If nobody is free you get an alert.

### C5. Run a rental
1. **Bookings -> open a confirmed booking -> Verify documents and hand over** (ticks that you saw the original NIC and licence). Status becomes Active.
2. **Return**: enter the return date and condition. A late return creates a fine; take it by cash or the customer pays by card or wallet. Then the booking is completed.
3. **Record cash payment** on a booking waiting for payment (customer paid at the counter).

### C6. Complaints and reviews
- **Complaints**: tabs by status. Open one, reply, set priority and status, add a resolution note.
- **Reviews**: reply publicly, hide or delete. Replies email the customer.

### C7. Email log and notifications
- **Email log**: every email with status; **Retry** failed ones.
- **Notifications**: the admin bell list; **Mark all read**.

## Part D - Turn on real email
Follow `docs/EMAIL-GUIDE.md`: create a Gmail app password, set `MAIL_USERNAME` and `MAIL_PASSWORD` as environment variables, set `app.mail.enabled=true`, restart. Register with a real address to test; the code arrives by email and the demo box disappears.

## Part E - Viva demo script (about 8 minutes)
1. Register a new customer -> read the code from the email (or `data/outbox/`) -> tick animation.
2. Book the **Toyota KDH** **with a driver**, Colombo to **Kandy** (show the one-way fee and live price lines).
3. Pay by **wallet** with PIN `0000` -> cross animation. Pay again with PIN `1234` -> tick, receipt, **Download PDF**.
4. Admin: dashboard -> booking -> show the **driver was assigned automatically**; open the driver's trip link from `data/outbox/` and press confirm.
5. Customer: book another vehicle, pay by **bank transfer** -> clock. Admin: **Payments -> Awaiting verification -> Approve** -> receipt email.
6. Customer: leave a **1-star review** -> admin bell alert -> admin **reply**.
7. Customer: file a **complaint** -> admin replies -> customer sees it.
8. Show `docs/class-diagram.md` diagram 1 and explain: inheritance (`Payment` subclasses), Strategy (`PaymentProcessor`, `PricingStrategy`, `LateFeePolicy`), Template Method (`DiscountPolicy`), events (`BookingConfirmedEvent`), file handling (`data/*.txt`).

## Part F - GitHub (10 marks)
1. Create a repository, add `.gitignore` containing `target/`, `data/`, `.idea/`.
2. `git init`, `git add .`, `git commit -m "Initial merged system"`, `git remote add origin <url>`, `git push -u origin main`.
3. Each member works on a branch for their package (see the split in the README), commits small and often under their own account, and opens a pull request.
4. Never commit `MAIL_PASSWORD`.
