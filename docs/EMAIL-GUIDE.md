# Sending real emails (free)

The app sends these emails: verification code, booking confirmed (with the **PDF receipt attached**), bank transfer received / rejected,
refund, driver assignment (to the driver with a trip link, and to the customer), complaint updates, review replies, and short **admin alerts**.
By default they are saved as text files in `data/outbox/` (PDFs too). To send real mail, switch on SMTP.
Every email, real or saved, is listed under **Admin, Email log** with its status (Sent / Saved / Failed) and a **Retry** button for failed ones.

## Option A - Gmail (free, easiest)
Limit: about 500 emails/day, plenty for a project.

1. Open https://myaccount.google.com/security and turn **2-Step Verification** ON.
2. Open https://myaccount.google.com/apppasswords , name it `VehicleRental`, click Create.
   Copy the 16-letter **App Password** (spaces do not matter). Your normal Gmail password will NOT work.
3. Put your details in IntelliJ environment variables (never in Git):
   Run -> Edit Configurations -> VehicleRentalApplication -> Environment variables:
   `MAIL_USERNAME=youraddress@gmail.com;MAIL_PASSWORD=abcdefghijklmnop`
4. In `src/main/resources/application.properties` change `app.mail.enabled=false` to `app.mail.enabled=true`.
5. Reload Maven (the project already contains `spring-boot-starter-mail`) and run the app.
6. Register a customer with an email you can open (the verification code arrives first), book a vehicle and pay. The confirmation and PDF receipt arrive in that inbox
   (check Spam the first time). If sending fails, the email is saved in `data/outbox/` instead and the console shows a warning.

## Option B - Brevo (free, 300 emails/day, no Gmail needed)
Create a free account at brevo.com -> SMTP & API -> generate an SMTP key, then use:
```
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
MAIL_USERNAME=<your Brevo login email>
MAIL_PASSWORD=<your SMTP key>
app.mail.from=<a sender address verified in Brevo>
```

## Option C - Mailtrap (free test inbox, nothing is really delivered)
Good for demos: mailtrap.io -> Email Testing -> copy host/port/user/password into the `spring.mail.*` properties.

## How it works (for the viva)
`EmailService` is the interface every service uses. `EmailDispatcher` implements it: it records the email in the log, then hands it to a
`MailTransport` (strategy). `FileMailTransport` writes to `data/outbox/`; `SmtpMailTransport` sends a real HTML email with attachments.
Spring picks the transport from `app.mail.enabled`. SMTP sending happens on a background thread so a slow mail server never blocks a booking,
and if SMTP fails the message is saved to the outbox and logged as Failed (it never crashes the request).
`EmailBuilder` / `EmailTemplates` build the HTML + plain text versions so all emails look the same.

Admin alert emails: `app.notify.admin-email=true` sends them to every administrator's email, or to one shared address in `app.notify.admin-address`.

## Troubleshooting
- `AuthenticationFailedException` : using the normal password instead of the App Password, or 2-Step Verification is off.
- Nothing arrives and the log says "saving to outbox": wrong credentials, no internet, or `MAIL_USERNAME` not set in the run configuration.
- School/campus Wi-Fi may block port 587: use a mobile hotspot.
