# Class diagrams, ER diagram and marking-guide mapping

Every diagram below is also saved as a ready-to-insert picture in `docs/diagrams/` (SVG for zooming, PNG for Word / PowerPoint).
The source of each is the `.mmd` file next to it - paste it into https://mermaid.live to edit.

| # | Diagram | Files |
|---|---------|-------|
| 1 | Payment and booking, with every relationship | `diagrams/01-payment-booking.svg / .png` |
| 2 | Complete domain model (all entities) | `diagrams/02-domain-model.svg / .png` |
| 3 | Layers: repositories, services, controllers | `diagrams/03-services-layers.svg / .png` |
| 4 | Email and notification classes | `diagrams/04-notification-email.svg / .png` |
| 5 | Booking life-cycle (states) | `diagrams/05-booking-states.svg / .png` |
| 6 | Payment sequence (what happens when you press Pay) | `diagrams/06-payment-sequence.svg / .png` |
| 7 | ER diagram (matches `database/database.sql`) | `diagrams/07-er.svg / .png` |

## 1. Payment and booking class diagram (all relationships)

Reading the arrows: hollow triangle = inheritance, dotted + hollow triangle = implements an interface,
filled diamond = composition (owns, cannot exist alone), hollow diamond = aggregation, plain arrow = association / uses,
dotted arrow = dependency (creates, throws, listens to).

```mermaid
classDiagram
direction LR

class BaseEntity {
  <<abstract>>
  -Long id
  -LocalDateTime createdAt
}

class Booking {
  -Long customerId
  -Long vehicleId
  -LocalDate pickupDate
  -LocalDate returnDate
  -LocalTime pickupTime
  -boolean withDriver
  -Long driverId
  -BigDecimal baseAmount
  -BigDecimal discountAmount
  -BigDecimal driverFee
  -BigDecimal oneWayFee
  -BigDecimal totalAmount
  -BigDecimal fineAmount
  -BookingStatus status
  -LocalDateTime holdExpiresAt
  +getReference() String
  +isOneWay() boolean
  +isDriverPending() boolean
  +isCancellableByCustomer(LocalDate) boolean
}
class BookingStatus {
  <<enumeration>>
  PENDING_PAYMENT
  AWAITING_VERIFICATION
  CONFIRMED
  ACTIVE
  FINE_DUE
  COMPLETED
  CANCELLED
}

class Payment {
  <<abstract>>
  -Long bookingId
  -Long customerId
  -BigDecimal amount
  -PaymentKind kind
  -PaymentStatus status
  -String reference
  -String receiptNo
  -LocalDateTime paidAt
  -String failureReason
  -String verifiedBy
  +getMethodType()* PaymentMethod
  +getMethodDetails()* String
  +isReceiptAvailable() boolean
}
class CardPayment {
  -String cardHolder
  -String cardLast4
  -String cardBrand
}
class CashPayment {
  -String receivedBy
}
class BankTransferPayment {
  -String bankName
  -String slipReference
  -LocalDate transferDate
}
class WalletPayment {
  -String provider
  -String mobileMasked
}
class PaymentMethod {
  <<enumeration>>
  CARD
  WALLET
  BANK_TRANSFER
  CASH
}
class PaymentStatus {
  <<enumeration>>
  PAID
  PENDING_VERIFICATION
  REJECTED
  FAILED
  REFUNDED
}
class PaymentKind {
  <<enumeration>>
  RENTAL
  FINE
}

class PaymentProcessor {
  <<interface>>
  +method() PaymentMethod
  +supportsFines() boolean
  +process(Booking, PaymentForm, BigDecimal, PaymentKind) Payment
}
class AbstractPaymentProcessor {
  <<abstract>>
  #prepare(Payment, Booking, BigDecimal, PaymentKind)
  #declined(Payment, String)
}
class CardPaymentProcessor
class WalletPaymentProcessor
class BankTransferProcessor
class PaymentForm {
  -PaymentMethod method
  -String cardNumber
  -String walletMobile
  -String slipReference
}
class PaymentFieldException {
  -Map errors
}

class PaymentService {
  +pay(customerId, bookingId, PaymentForm) Payment
  +recordCash(bookingId, adminName) Payment
  +approve(paymentId, adminName) Payment
  +reject(paymentId, adminName, reason) Payment
  +onBookingCancelled(BookingCancelledEvent)
  +receiptPdf(Payment) byte[]
}
class ReceiptService {
  +build(Payment) Receipt
  +pdf(Payment) byte[]
  +attachment(Payment) EmailAttachment
}
class Receipt {
  -String receiptNo
  -String statusCode
  -List~ReceiptLine~ lines
  -BigDecimal total
}
class ReceiptLine {
  -String label
  -String detail
  -BigDecimal amount
}
class ReceiptPdfGenerator {
  +generate(Receipt) byte[]
}

class BookingService {
  +createBooking(customerId, BookingForm) Booking
  +checkAvailability(...) Availability
  +markPaid(id) Booking
  +markAwaitingVerification(id) Booking
  +confirmVerified(id) Booking
  +rejectVerification(id) Booking
  +processReturn(id, ReturnForm) Booking
  +cancelByCustomer(id, customerId) Booking
}
class PricingService {
  +quote(Vehicle, CustomerTier, pickup, days, withDriver, Location, Location) PriceQuote
}
class PricingStrategy {
  <<interface>>
  +calculate(Vehicle, long days) BigDecimal
}
class StandardPricing
class PremiumPricing
class DiscountPolicy {
  <<abstract>>
  +discountFor(BigDecimal, days, pickup) BigDecimal
  #isApplicable(days, pickup)* boolean
  #rate()* BigDecimal
  +describe()* String
}
class LongTermDiscount
class WeekendDiscount
class PriceQuote {
  -List~PriceLine~ lines
  -BigDecimal total
}
class PriceLine {
  -String label
  -BigDecimal amount
}
class LateFeePolicy {
  <<interface>>
  +calculateFine(BigDecimal, long lateDays) BigDecimal
  +describe() String
}
class PercentPerDayLateFeePolicy
class PremiumLateFeePolicy

class Customer {
  -CustomerTier tier
  -boolean emailVerified
}
class Vehicle {
  <<abstract>>
  -BigDecimal pricePerDay
  +isDriverAvailable() boolean
}
class Location
class Driver
class DriverAssignment
class EmailService {
  <<interface>>
  +send(EmailMessage) EmailStatus
}
class NotificationService
class BookingConfirmedEvent
class BookingCancelledEvent

BaseEntity <|-- Booking
BaseEntity <|-- Payment
Payment <|-- CardPayment
Payment <|-- CashPayment
Payment <|-- BankTransferPayment
Payment <|-- WalletPayment

PaymentProcessor <|.. AbstractPaymentProcessor
AbstractPaymentProcessor <|-- CardPaymentProcessor
AbstractPaymentProcessor <|-- WalletPaymentProcessor
AbstractPaymentProcessor <|-- BankTransferProcessor
PricingStrategy <|.. StandardPricing
PricingStrategy <|.. PremiumPricing
DiscountPolicy <|-- LongTermDiscount
DiscountPolicy <|-- WeekendDiscount
LateFeePolicy <|.. PercentPerDayLateFeePolicy
LateFeePolicy <|.. PremiumLateFeePolicy

Customer "1" --> "0..*" Booking : makes
Vehicle "1" --> "0..*" Booking : is rented in
Booking "1" --> "0..*" Payment : paid by
Customer "1" --> "0..*" Payment : pays
Booking "0..*" --> "0..1" Location : pickup
Booking "0..*" --> "0..1" Location : drop-off
Booking "0..*" --> "0..1" Driver : driven by
Booking "1" --> "0..*" DriverAssignment : has
Driver "1" --> "0..*" DriverAssignment : takes
Booking --> BookingStatus
Payment --> PaymentStatus
Payment --> PaymentKind
Payment ..> PaymentMethod : getMethodType

PaymentService o-- "3" PaymentProcessor : strategies
PaymentService ..> PaymentForm
PaymentService ..> PaymentFieldException : propagates
PaymentProcessor ..> PaymentFieldException : throws
PaymentService --> BookingService
PaymentService --> ReceiptService
PaymentService --> EmailService
PaymentService --> NotificationService
ReceiptService ..> Receipt : builds
Receipt "1" *-- "1..*" ReceiptLine
ReceiptService --> ReceiptPdfGenerator
ReceiptPdfGenerator ..> Receipt

BookingService --> PricingService
BookingService --> LateFeePolicy : per tier
BookingService ..> BookingConfirmedEvent : publishes
BookingService ..> BookingCancelledEvent : publishes
PaymentService ..> BookingCancelledEvent : listens, refunds
PricingService o-- PricingStrategy
PricingService o-- "*" DiscountPolicy
PricingService ..> PriceQuote : returns
PriceQuote "1" *-- "1..*" PriceLine
```

## 2. Complete domain model

```mermaid
classDiagram
direction TB

class BaseEntity {
  <<abstract>>
  -Long id
  -LocalDateTime createdAt
}
class Person {
  <<abstract>>
  -String fullName
  -String email
  -String phone
  -String passwordHash
  +getRole()* String
  +isLoginAllowed() boolean
}
class Customer {
  -String address
  -String nic
  -String licenceId
  -UserStatus status
  -boolean emailVerified
  -CustomerTier tier
}
class Admin {
  -String username
}
class CustomerTier {
  <<enumeration>>
  STANDARD
  PREMIUM
}
class EmailVerification {
  -Long customerId
  -String codeHash
  -LocalDateTime expiresAt
  -int attempts
  -LocalDateTime usedAt
}

class Vehicle {
  <<abstract>>
  -String brand
  -String model
  -int year
  -Transmission transmission
  -String registrationNo
  -BigDecimal pricePerDay
  -FuelType fuelType
  -int seatingCapacity
  -VehicleStatus status
  -String imageUrl
  +getType()* VehicleType
  +getCapacityInfo()* String
  +isDriverAvailable() boolean
  +getFullName() String
}
class Car {
  -int doors
  -int bootLitres
}
class Van {
  -int cargoKg
  -boolean airConditioned
}
class Bike {
  -int engineCc
  -boolean helmetProvided
}
class Truck {
  -int maxLoadKg
  -String bodyKind
}

class Location {
  -String name
  -String city
  -String address
  -boolean active
}
class Driver {
  -String fullName
  -String phone
  -String licenceNo
  -DriverStatus status
}
class DriverAssignment {
  -Long bookingId
  -Long driverId
  -AssignmentStatus status
  -String token
}

class Booking {
  -LocalDate pickupDate
  -LocalDate returnDate
  -boolean withDriver
  -BigDecimal totalAmount
  -BookingStatus status
}
class Payment {
  <<abstract>>
  -BigDecimal amount
  -PaymentStatus status
  -String receiptNo
}
class CardPayment
class CashPayment
class BankTransferPayment
class WalletPayment

class Review {
  -int rating
  -String comment
  -boolean hidden
  -String adminReply
  +isLow() boolean
}
class Complaint {
  -ComplaintCategory category
  -String subject
  -ComplaintStatus status
  -ComplaintPriority priority
  +getReference() String
}
class ComplaintReply {
  -String authorRole
  -String message
}
class Notification {
  -String recipientRole
  -NotificationType type
  -String title
  -boolean read
}
class EmailLog {
  -String toAddress
  -EmailType type
  -EmailStatus status
}

BaseEntity <|-- Person
Person <|-- Customer
Person <|-- Admin
BaseEntity <|-- Vehicle
Vehicle <|-- Car
Vehicle <|-- Van
Vehicle <|-- Bike
Vehicle <|-- Truck
BaseEntity <|-- Booking
BaseEntity <|-- Payment
Payment <|-- CardPayment
Payment <|-- CashPayment
Payment <|-- BankTransferPayment
Payment <|-- WalletPayment
BaseEntity <|-- Location
BaseEntity <|-- Driver
BaseEntity <|-- DriverAssignment
BaseEntity <|-- Review
BaseEntity <|-- Complaint
BaseEntity <|-- ComplaintReply
BaseEntity <|-- Notification
BaseEntity <|-- EmailLog
BaseEntity <|-- EmailVerification

Customer --> CustomerTier
Customer "1" --> "0..*" EmailVerification : verifies with
Customer "1" --> "0..*" Booking : makes
Vehicle "1" --> "0..*" Booking : rented in
Booking "0..*" --> "0..1" Location : pickup
Booking "0..*" --> "0..1" Location : drop-off
Booking "0..*" --> "0..1" Driver : driven by
Driver "1" --> "0..*" DriverAssignment
Booking "1" --> "0..*" DriverAssignment
Booking "1" --> "0..*" Payment
Customer "1" --> "0..*" Payment
Booking "1" --> "0..1" Review : reviewed once
Customer "1" --> "0..*" Review : writes
Vehicle "1" --> "0..*" Review : about
Customer "1" --> "0..*" Complaint : files
Booking "0..1" <-- "0..*" Complaint : about
Complaint "1" *-- "0..*" ComplaintReply
Customer "1" --> "0..*" Notification : receives
Admin "1" --> "0..*" Notification : shared alerts
```

## 3. Layers

```mermaid
classDiagram
direction LR

class CrudRepository~T~ {
  <<interface>>
  +create(T) T
  +findById(Long) Optional~T~
  +findAll() List~T~
  +update(T) T
  +deleteById(Long) boolean
  +count() long
}
class FileRepository~T~ {
  <<abstract>>
  #toFields(T)* String[]
  #fromFields(String[])* T
  #headerComment()* String
}
CrudRepository~T~ <|.. FileRepository~T~

class UserRepository
class VehicleRepository
class BookingRepository
class PaymentRepository
class ReviewRepository
class LocationRepository
class DriverRepository
class DriverAssignmentRepository
class ComplaintRepository
class ComplaintReplyRepository
class NotificationRepository
class EmailLogRepository
class EmailVerificationRepository
class AdminRepository
FileRepository~T~ <|-- UserRepository
FileRepository~T~ <|-- VehicleRepository
FileRepository~T~ <|-- BookingRepository
FileRepository~T~ <|-- PaymentRepository
FileRepository~T~ <|-- ReviewRepository
FileRepository~T~ <|-- LocationRepository
FileRepository~T~ <|-- DriverRepository
FileRepository~T~ <|-- DriverAssignmentRepository
FileRepository~T~ <|-- ComplaintRepository
FileRepository~T~ <|-- ComplaintReplyRepository
FileRepository~T~ <|-- NotificationRepository
FileRepository~T~ <|-- EmailLogRepository
FileRepository~T~ <|-- EmailVerificationRepository
FileRepository~T~ <|-- AdminRepository

class UserService
class VerificationService
class VehicleService
class LocationService
class BookingService
class PricingService
class PaymentService
class ReceiptService
class DriverService
class ReviewService
class ComplaintService
class NotificationService
class DashboardService

UserService --> UserRepository
VerificationService --> EmailVerificationRepository
VerificationService --> UserService
VehicleService --> VehicleRepository
LocationService --> LocationRepository
BookingService --> BookingRepository
BookingService --> VehicleService
BookingService --> UserService
BookingService --> LocationService
BookingService --> PricingService
PaymentService --> PaymentRepository
PaymentService --> BookingService
PaymentService --> ReceiptService
ReceiptService --> BookingService
ReceiptService --> VehicleService
DriverService --> DriverRepository
DriverService --> DriverAssignmentRepository
DriverService ..> BookingService : lazy
ReviewService --> ReviewRepository
ReviewService --> BookingService
ComplaintService --> ComplaintRepository
ComplaintService --> ComplaintReplyRepository
ComplaintService --> BookingService
NotificationService --> NotificationRepository
NotificationService --> AdminRepository
DashboardService --> BookingService
DashboardService --> PaymentService
DashboardService --> ComplaintService
DashboardService --> DriverService
DashboardService --> ReviewService

class AuthController
class VerificationController
class VehicleController
class BookingController
class PaymentController
class DriverTripController
class ReviewController
class ComplaintController
class NotificationController
class AdminVehicleController
class AdminBookingController
class AdminPaymentController
class AdminDriverController
class AdminLocationController
class AdminReviewController
class AdminComplaintController
class AdminUserController
class AdminEmailController
class AdminDashboardController

AuthController --> UserService
AuthController --> VerificationService
VerificationController --> VerificationService
VehicleController --> VehicleService
VehicleController --> BookingService
BookingController --> BookingService
PaymentController --> PaymentService
DriverTripController --> DriverService
ReviewController --> ReviewService
ComplaintController --> ComplaintService
NotificationController --> NotificationService
AdminVehicleController --> VehicleService
AdminBookingController --> BookingService
AdminBookingController --> DriverService
AdminPaymentController --> PaymentService
AdminDriverController --> DriverService
AdminLocationController --> LocationService
AdminReviewController --> ReviewService
AdminComplaintController --> ComplaintService
AdminUserController --> UserService
AdminEmailController --> EmailLogRepository
AdminDashboardController --> DashboardService
```

## 4. Email and notifications

```mermaid
classDiagram
direction LR
class EmailService {
  <<interface>>
  +send(EmailMessage) EmailStatus
  +send(to, subject, body) EmailStatus
  +isDeliveringRealMail() boolean
}
class EmailDispatcher {
  -boolean async
  +send(EmailMessage) EmailStatus
  +resend(Long logId) EmailStatus
}
class MailTransport {
  <<interface>>
  +deliver(EmailMessage)
}
class FileMailTransport {
  saves to data/outbox
}
class SmtpMailTransport {
  MIME HTML + PDF attachments
}
class EmailMessage {
  -String to
  -String subject
  -String htmlBody
  -EmailType type
  +withAttachment(EmailAttachment) EmailMessage
}
class EmailAttachment {
  -String filename
  -byte[] content
  +pdf(name, bytes)$ EmailAttachment
}
class EmailBuilder {
  +greeting(String)
  +paragraph(String)
  +highlight(label, value)
  +fact(label, value)
  +button(label, path)
  +build(to, EmailType) EmailMessage
}
class EmailTemplates {
  +builder(title) EmailBuilder
}
class EmailLog
class EmailLogRepository
class EmailType {
  <<enumeration>>
  VERIFICATION
  BOOKING
  PAYMENT
  RECEIPT
  DRIVER
  COMPLAINT
  REVIEW
  ADMIN_ALERT
  GENERAL
}
class EmailStatus {
  <<enumeration>>
  QUEUED
  SENT
  SAVED
  FAILED
}
class NotificationService {
  +notifyAdmins(type, title, message, link)
  +notifyCustomer(id, type, title, message, link)
  +markRead(...)
  +unreadForAdmins() long
}
class Notification
class NotificationType {
  <<enumeration>>
  BOOKING
  PAYMENT
  DRIVER
  COMPLAINT
  REVIEW
  ACCOUNT
  SYSTEM
}

EmailService <|.. EmailDispatcher
MailTransport <|.. FileMailTransport
MailTransport <|.. SmtpMailTransport
EmailDispatcher --> MailTransport : strategy
EmailDispatcher --> EmailLogRepository
EmailLogRepository ..> EmailLog
EmailDispatcher ..> FileMailTransport : fallback
EmailMessage "1" *-- "0..*" EmailAttachment
EmailTemplates ..> EmailBuilder : creates
EmailBuilder ..> EmailMessage : builds
EmailMessage --> EmailType
EmailLog --> EmailStatus
NotificationService --> EmailService : admin alert mail
NotificationService --> EmailTemplates
NotificationService ..> Notification
Notification --> NotificationType
```

## 5. Booking life-cycle

```mermaid
stateDiagram-v2
direction LR
[*] --> PENDING_PAYMENT : customer books (15 min hold)
PENDING_PAYMENT --> CONFIRMED : card / wallet / cash paid
PENDING_PAYMENT --> AWAITING_VERIFICATION : bank transfer submitted
PENDING_PAYMENT --> CANCELLED : hold expired or cancelled
AWAITING_VERIFICATION --> CONFIRMED : admin approves transfer
AWAITING_VERIFICATION --> PENDING_PAYMENT : admin rejects transfer (new hold)
AWAITING_VERIFICATION --> CANCELLED : cancelled
CONFIRMED --> ACTIVE : admin verifies NIC and licence at pickup
CONFIRMED --> CANCELLED : cancelled, payment refunded, driver released
ACTIVE --> COMPLETED : returned on time
ACTIVE --> FINE_DUE : returned late
FINE_DUE --> COMPLETED : fine paid (card or wallet)
COMPLETED --> [*]
CANCELLED --> [*]
```

## 6. Payment sequence

```mermaid
sequenceDiagram
autonumber
actor C as Customer
participant UI as Payment page (JS)
participant PC as PaymentController
participant PS as PaymentService
participant PP as PaymentProcessor (strategy)
participant BS as BookingService
participant RS as ReceiptService
participant ES as EmailService
participant NS as NotificationService
actor A as Admin
C->>UI: choose method, fill details, press Pay
UI->>UI: show "Verifying your payment" overlay
UI->>PC: POST /payments/booking/{id}/process
PC->>PS: pay(customerId, bookingId, form)
PS->>PP: process(booking, form, amount, kind)
alt invalid details
  PP-->>PS: PaymentFieldException(errors)
  PS-->>UI: JSON ok=false + field errors (overlay shows cross)
else card or wallet approved
  PP-->>PS: Payment PAID
  PS->>BS: markPaid() -> CONFIRMED (+ BookingConfirmedEvent)
  PS->>RS: attachment(payment) PDF
  PS->>ES: booking confirmed + receipt PDF
  PS->>NS: notify customer and admins
  PS-->>UI: JSON ok=true PAID (overlay shows tick)
else declined
  PP-->>PS: Payment FAILED + reason
  PS-->>UI: JSON ok=false (overlay shows cross)
else bank transfer
  PP-->>PS: Payment PENDING_VERIFICATION
  PS->>BS: markAwaitingVerification()
  PS->>NS: alert admins "transfer to verify"
  PS-->>UI: JSON ok=true PENDING (overlay shows clock)
  A->>PS: approve(paymentId) or reject(paymentId, reason)
  PS->>BS: confirmVerified() or rejectVerification()
  PS->>ES: receipt PDF, or rejection email
end
UI->>C: redirect to receipt page (download PDF)
```

## 7. ER diagram

```mermaid
erDiagram
  CUSTOMERS ||--o{ BOOKINGS : makes
  CUSTOMERS ||--o{ EMAIL_VERIFICATIONS : verifies
  CUSTOMERS ||--o{ PAYMENTS : pays
  CUSTOMERS ||--o{ REVIEWS : writes
  CUSTOMERS ||--o{ COMPLAINTS : files
  VEHICLES ||--o{ BOOKINGS : "rented in"
  VEHICLES ||--o{ REVIEWS : "about"
  LOCATIONS ||--o{ BOOKINGS : "pickup / drop-off"
  DRIVERS ||--o{ BOOKINGS : drives
  DRIVERS ||--o{ DRIVER_ASSIGNMENTS : takes
  BOOKINGS ||--o{ DRIVER_ASSIGNMENTS : has
  BOOKINGS ||--o{ PAYMENTS : "paid by"
  BOOKINGS ||--o| REVIEWS : "reviewed once"
  BOOKINGS ||--o{ COMPLAINTS : "subject of"
  COMPLAINTS ||--o{ COMPLAINT_REPLIES : thread
  CUSTOMERS {
    bigint id PK
    string email UK
    enum tier "STANDARD, PREMIUM"
    bool email_verified
  }
  VEHICLES {
    bigint id PK
    enum type "CAR, VAN, BIKE, TRUCK"
    string registration_no UK
    decimal price_per_day
  }
  BOOKINGS {
    bigint id PK
    bigint customer_id FK
    bigint vehicle_id FK
    bigint pickup_location_id FK
    bigint drop_location_id FK
    bigint driver_id FK
    bool with_driver
    decimal total_amount
    enum status
  }
  PAYMENTS {
    bigint id PK
    bigint booking_id FK
    enum method "CARD, CASH, BANK, WALLET"
    enum status "PAID, PENDING_VERIFICATION, REJECTED, FAILED, REFUNDED"
    string receipt_no
  }
  REVIEWS {
    bigint id PK
    int rating "1 to 5"
    string admin_reply
  }
  NOTIFICATIONS {
    bigint id PK
    enum recipient_role
    bool is_read
  }
  EMAIL_LOG {
    bigint id PK
    enum type
    enum status
  }
```

## 8. Where each OOP concept is used (marking guide: 20 marks)

| Concept | Where |
|---------|-------|
| Encapsulation | Private fields with getters / setters in every entity; `Customer.setEmail` lower-cases, `Payment.setAmount` rejects negatives |
| Inheritance | `BaseEntity -> Person -> Customer / Admin`; `Vehicle -> Car / Van / Bike / Truck`; `Payment -> CardPayment / CashPayment / BankTransferPayment / WalletPayment`; `FileRepository<T> -> 14 repositories` |
| Polymorphism | `Vehicle.getCapacityInfo()` and `isDriverAvailable()`; `Payment.getMethodType()` / `getMethodDetails()`; `PricingStrategy`, `LateFeePolicy`, `PaymentProcessor`, `MailTransport` |
| Abstraction | `CrudRepository<T>`, `FileRepository<T>`, `EmailService`, `PaymentProcessor`, abstract `Vehicle`, `Payment`, `DiscountPolicy` |
| Strategy pattern | `PaymentProcessor` (card / wallet / bank), `PricingStrategy` (standard / premium), `LateFeePolicy` (per tier), `MailTransport` (file / SMTP) |
| Template Method | `DiscountPolicy.discountFor()` is final and calls the subclass hooks `isApplicable()` and `rate()`; `FileRepository` calls `toFields` / `fromFields` |
| Loose coupling (events) | `BookingConfirmedEvent` -> driver assignment; `BookingCancelledEvent` -> refund and driver release |

## 9. CRUD coverage (marking guide: 30 marks)

| Entity | Create | Read | Update | Delete |
|--------|--------|------|--------|--------|
| Customer | register + verify email | profile, admin list | edit profile, admin edit (tier, verified, block) | admin delete, own account delete |
| Vehicle (4 types) | admin form | catalogue, search, details | admin form | admin delete |
| Location | admin | admin list, booking drop-downs | admin | admin |
| Driver | admin | admin list | admin (on duty / off duty) | admin |
| Booking | booking form | my orders, admin list | change renter details, status changes | admin delete, customer cancel |
| Payment | pay online, admin cash | history, receipt, admin tabs | approve / reject transfer, refund | admin delete |
| Review | after a completed rental | public list, my reviews | edit, admin reply, hide | delete |
| Complaint | complaint form | my complaints, admin tabs | reply, status, priority, reopen | admin delete |
| Notification | automatic | bell list | mark read | trimmed automatically |

## 10. File handling (marking guide: 10 marks)

One text file per entity in `data/`, one record per line, `|` separated, special characters escaped. Writes go to a temp file and
are moved into place (never half written); reads come from a memory copy loaded once. New columns are appended at the end so old
files still load. Emails are saved in `data/outbox/` (with the PDF receipt) when SMTP is off, and every email is listed in `data/email_log.txt`.
