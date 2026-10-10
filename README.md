# Vehicle Rental Service

Spring Boot 3 + Thymeleaf + MySQL (or JSON-file storage) — Java 17.

## Run
1. Start MySQL (user `root`, password `1234`).
2. From the project root:  `mvn spring-boot:run`
   - database `vrs` and all tables are created automatically (`database/database.sql`)
   - sample data is seeded on first run
3. Open <http://localhost:8080>

| Role     | Username | Password  |
|----------|----------|-----------|
| Admin    | admin    | admin123  |
| Customer | nimal    | nimal123  |
| Customer | kasun    | kasun123  |

No MySQL? Set `app.storage=file` in `application.properties`.

## Package layout (`com.vehiclerental`)
Each feature module has `entity / repository / service / controller / dto`:
`user`, `vehicle`, `booking`, `payment`, `admin`, `review`.
UI: the new photo-based Thymeleaf UI (templates in `templates/`, `static/css/style.css`, vehicle photos in `static/images/`).
`/` redirects to `/vehicles`, which acts as the landing page.
Shared code: `common` (storage abstraction, CRUD base classes, utils), `config`, `exception`.
