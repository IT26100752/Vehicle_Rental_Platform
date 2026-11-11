-- Sample data for the Vehicle Rental Management System.  Run AFTER database.sql
--   mysql -u root -p vehicle_rental < database/sample-data.sql
-- Logins:  admin / Admin@123    kasun@rental.lk / Demo@123 (standard)    premium@rental.lk / Premium@123 (premium)
-- Passwords are stored as BCrypt hashes (same format Spring's BCryptPasswordEncoder produces).
USE vehicle_rental;

INSERT INTO admins (username, full_name, email, phone, password_hash) VALUES
('admin', 'System Administrator', 'admin@vehiclerental.lk', '0112345678', '$2a$10$OqQsteHMDplMrwpShEufreayRYkH.GPH.2VJSAE15oEwC3hsBwU0W');

INSERT INTO customers (full_name, email, phone, password_hash, address, status, email_verified, tier) VALUES
('Kasun Perera',  'kasun@rental.lk',    '0771234567', '$2a$10$XpGYxPYZRBJWUw2T3glwce6ceBXEziVOJoGQUFHgMK3VrmryZhLOW', 'Colombo 07', 'ACTIVE', TRUE, 'STANDARD'),
('Nimali Perera',  'premium@rental.lk', '0712345678', '$2a$10$lz9ihEskhQfyZN6cwJLQp.QAk7qzqtiP6OyLjrRJkqGX.l.fgZdfm', 'Kandy',      'ACTIVE', TRUE, 'PREMIUM');

INSERT INTO locations (name, city, address, phone, active, latitude, longitude) VALUES
('Colombo Fort',         'Colombo',    '12 Olcott Mawatha, Colombo 01',  '0112000001', TRUE, 6.934400, 79.842800),
('Bandaranaike Airport', 'Katunayake', 'Arrivals hall, BIA',             '0112000002', TRUE, 7.180800, 79.884100),
('Kandy City',           'Kandy',      '88 Peradeniya Road, Kandy',      '0812000003', TRUE, 7.290600, 80.633700),
('Galle Fort',           'Galle',      '5 Church Street, Galle',         '0912000004', TRUE, 6.026700, 80.217000),
('Negombo Beach',        'Negombo',    '30 Lewis Place, Negombo',        '0312000005', TRUE, 7.208300, 79.835800);

INSERT INTO drivers (full_name, phone, email, licence_no, languages, status) VALUES
('Kasun Fernando',   '0771110001', 'kasun.driver@rental.lk', 'B2345671', 'Sinhala, English',          'AVAILABLE'),
('Ravindran Kumar',  '0771110002', 'ravi.driver@rental.lk',  'B2345672', 'Tamil, Sinhala, English',   'AVAILABLE'),
('Sunil Jayasuriya', '0771110003', 'sunil.driver@rental.lk', 'B2345673', 'Sinhala',                   'OFF_DUTY');

INSERT INTO vehicles (type, brand, model, model_year, transmission, registration_no, price_per_day, fuel_type, seating_capacity, status, image_url, description,
                      doors, boot_litres, cargo_kg, air_conditioned, engine_cc, helmet_provided, max_load_kg, body_kind) VALUES
('CAR',  'Toyota',     'Axio',    2018, 'AUTOMATIC', 'CAD-7783', 8800.00,  'PETROL',   5,  'AVAILABLE',   '/images/vehicles/axio.jpg',    'Reliable family sedan with a large boot.',                      4, 450, NULL, NULL, NULL, NULL, NULL, NULL),
('CAR',  'Nissan',     'Leaf',    2019, 'AUTOMATIC', 'EV-1180',  11000.00, 'ELECTRIC', 5,  'RENTED',      '/images/vehicles/leaf.jpg',    'Quiet, zero-emission electric car.',                            4, 370, NULL, NULL, NULL, NULL, NULL, NULL),
('CAR',  'Honda',      'City',    2020, 'AUTOMATIC', 'CBK-3345', 10500.00, 'PETROL',   5,  'AVAILABLE',   '/images/photos/car.jpg',       'Smooth silver sedan, ideal for business trips.',                4, 400, NULL, NULL, NULL, NULL, NULL, NULL),
('VAN',  'Toyota',     'KDH',     2017, 'AUTOMATIC', 'NC-4410',  21000.00, 'DIESEL',   10, 'AVAILABLE',   '/images/vehicles/kdh.jpg',     'Comfortable 10-seater, popular for family trips.',              NULL, NULL, 600, TRUE, NULL, NULL, NULL, NULL),
('VAN',  'Nissan',     'Caravan', 2016, 'MANUAL',    'NB-1377',  18500.00, 'DIESEL',   9,  'AVAILABLE',   '/images/vehicles/caravan.jpg', 'Roomy 9-seater with lots of luggage space.',                    NULL, NULL, 800, TRUE, NULL, NULL, NULL, NULL),
('VAN',  'Ford',       'Transit', 2019, 'MANUAL',    'NB-8821',  23000.00, 'DIESEL',   14, 'AVAILABLE',   '/images/photos/van.jpg',       '14-seat tourist van with powerful A/C.',                        NULL, NULL, 700, TRUE, NULL, NULL, NULL, NULL),
('BIKE', 'Yamaha',     'FZ V3',   2021, 'MANUAL',    'BKA-2204', 3500.00,  'PETROL',   2,  'AVAILABLE',   '/images/vehicles/fz.jpg',      'Light, nimble and thrifty. Helmet included.',                   NULL, NULL, NULL, NULL, 150, TRUE, NULL, NULL),
('BIKE', 'Ducati',     'Monster', 2020, 'MANUAL',    'BKB-9001', 9500.00,  'PETROL',   2,  'MAINTENANCE', '/images/photos/bike.jpg',      'Powerful 800cc sport bike for experienced riders.',             NULL, NULL, NULL, NULL, 800, TRUE, NULL, NULL),
('TRUCK','Mitsubishi', 'Canter',  2018, 'MANUAL',    'LD-5530',  16000.00, 'DIESEL',   3,  'AVAILABLE',   '/images/vehicles/canter.jpg',  'Reliable light truck for moving house or deliveries.',          NULL, NULL, NULL, NULL, NULL, NULL, 3000, 'Flatbed'),
('TRUCK','Isuzu',      'NPR',     2017, 'MANUAL',    'LD-6618',  19500.00, 'DIESEL',   3,  'AVAILABLE',   '/images/photos/truck.jpg',     'Box truck with a roomy, weather-proof body.',                   NULL, NULL, NULL, NULL, NULL, NULL, 4000, 'Box');

-- A paid with-driver, one-way trip (Colombo -> Kandy) with its driver, payment and receipt
INSERT INTO bookings (customer_id, vehicle_id, vehicle_model, vehicle_reg_no, pickup_date, return_date, pickup_time,
                      pickup_location_id, pickup_location, drop_location_id, drop_location, with_driver, driver_id, driver_name, driver_phone,
                      renter_name, nic, sex, age, licence_id, price_per_day, base_amount, discount_amount, driver_fee, one_way_fee, total_amount, status)
VALUES (2, 4, 'Toyota KDH', 'NC-4410', CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 6 DAY, '09:00',
        1, 'Colombo Fort - Colombo', 3, 'Kandy City - Kandy', TRUE, 1, 'Kasun Fernando', '0771110001',
        'Nimali Perera', '200012345678', 'Female', 29, '', 21000.00, 63000.00, 6300.00, 10500.00, 2500.00, 69700.00, 'CONFIRMED');

INSERT INTO driver_assignments (booking_id, driver_id, pickup_date, return_date, status, token, notified_at)
VALUES (1, 1, CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 6 DAY, 'ASSIGNED', 'demo0123456789abcdef0123456789ab', NOW());

INSERT INTO payments (booking_id, customer_id, method, kind, amount, status, reference, receipt_no, paid_at, card_holder, card_last4, card_brand)
VALUES (1, 2, 'CARD', 'RENTAL', 69700.00, 'PAID', 'PAY-SEED0001', CONCAT('RCT-', YEAR(CURDATE()), '-00001'), NOW(), 'Nimali Perera', '4242', 'Visa');

-- A bank transfer waiting for the admin to verify it
INSERT INTO bookings (customer_id, vehicle_id, vehicle_model, vehicle_reg_no, pickup_date, return_date, pickup_time,
                      pickup_location_id, pickup_location, drop_location_id, drop_location, with_driver,
                      renter_name, nic, sex, age, licence_id, price_per_day, base_amount, total_amount, status)
VALUES (1, 5, 'Nissan Caravan', 'NB-1377', CURDATE() + INTERVAL 8 DAY, CURDATE() + INTERVAL 10 DAY, '09:00',
        4, 'Galle Fort - Galle', 4, 'Galle Fort - Galle', FALSE,
        'Kasun Perera', '200012345678', 'Male', 25, 'B1234567', 18500.00, 37000.00, 37000.00, 'AWAITING_VERIFICATION');

INSERT INTO payments (booking_id, customer_id, method, kind, amount, status, reference, bank_name, slip_reference, transfer_date)
VALUES (2, 1, 'BANK', 'RENTAL', 37000.00, 'PENDING_VERIFICATION', 'PAY-SEED0002', 'Sampath Bank', 'TXN884201', CURDATE());

-- A low review (still published) and a complaint
INSERT INTO bookings (customer_id, vehicle_id, vehicle_model, vehicle_reg_no, pickup_date, return_date, actual_return_date, pickup_time,
                      renter_name, nic, sex, age, licence_id, price_per_day, base_amount, discount_amount, total_amount, status, docs_verified, verified_by)
VALUES (2, 9, 'Mitsubishi Canter', 'LD-5530', CURDATE() - INTERVAL 12 DAY, CURDATE() - INTERVAL 9 DAY, CURDATE() - INTERVAL 9 DAY, '09:00',
        'Nimali Perera', '200012345678', 'Female', 29, 'B7654321', 16000.00, 48000.00, 4800.00, 43200.00, 'COMPLETED', TRUE, 'admin');

INSERT INTO reviews (booking_id, customer_id, customer_name, vehicle_id, vehicle_model, rating, comment)
VALUES (3, 2, 'Nimali Perera', 9, 'Mitsubishi Canter', 2, 'The truck was not as clean as I expected and pickup was 40 minutes late.');

INSERT INTO complaints (customer_id, customer_name, booking_id, category, subject, description, status, priority)
VALUES (2, 'Nimali Perera', 3, 'STAFF_SERVICE', 'Late handover at Kandy', 'The truck was handed over 40 minutes late and the cabin had not been cleaned.', 'OPEN', 'MEDIUM');
