# Email verification (6-digit code)

## Flow
1. `POST /register` creates the customer with `emailVerified=false`, `VerificationService.issue()` emails a code, and the browser goes to `GET /verify-email?email=...`.
2. The customer types the 6 digits (auto-advance, paste supported) -> `POST /verify-email`.
3. Correct code -> the account is marked verified and the same page plays the **tick animation**, then forwards to the sign-in page.
4. `POST /verify-email/resend` sends a fresh code (older codes stop working) after a cool-down.
5. Signing in with an unverified account is refused (`EmailNotVerifiedException`) and the login page offers an "Enter verification code" button.
6. Registering again with an email that is still unverified refreshes the details and password and issues a new code instead of saying "already registered".

Customers created before this feature (rows without the column) and the seeded demo accounts count as verified.

## Security choices
- Code made with `SecureRandom`; only its **BCrypt hash** is stored (`data/verifications.txt`).
- Expires after `app.verify.code-minutes` (15).
- Wrong guesses limited by `app.verify.max-attempts` (5), then a new code is required.
- Resend cool-down `app.verify.resend-seconds` (60).
- A used code is marked `usedAt` and can never be used twice.

## Without SMTP
With `app.mail.enabled=false` the email is written to `data/outbox/` and the code is also printed in the console where the app runs. The code is never shown on the web page. Real SMTP (`app.mail.enabled=true`) delivers it to the inbox.

## Classes
`Customer.emailVerified`, `EmailVerification`, `EmailVerificationRepository`, `VerificationService`, `VerificationController`, `EmailNotVerifiedException`; `AuthController` orchestrates register -> issue.
