-- =====================================================================
-- Vehicle Rental Service - OPTIONAL sample data (same data the app seeds itself)
-- The app seeds this automatically when the tables are empty, so you only
-- need this file if you want to load the data by hand:
--     mysql -u root -p1234 < database/sample-data.sql
-- Logins:  admin/admin123   nimal/nimal123   kasun/kasun123
-- Passwords are stored as SHA-256 hashes (never plain text).
-- =====================================================================
USE vrs;

INSERT IGNORE INTO users (id,type,full_name,username,password_hash,email,phone,nic,address,premium,department,permission_level) VALUES
(1,'ADMIN','Deshan Perera','admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','admin@vrs.lk','0771000000',NULL,NULL,NULL,'Operations',3),
(2,'CUSTOMER','Nimal Silva','nimal','ad27266cd9aa5c55922c709207cfe69ae0544af6c66bb739fa76271481f5b904','nimal@gmail.com','0771234567','940001234V','Colombo',TRUE,NULL,NULL),
(3,'CUSTOMER','Kasun Fernando','kasun','564303e311e960c11f07c28fc1f0331b8b2f1ac74a8f75c18562db297226b5af','kasun@gmail.com','0712223344','980005678V','Kandy',FALSE,NULL,NULL);

INSERT IGNORE INTO vehicles (id,vehicle_type,vehicle_number,brand,model,year,price_per_day,fuel_type,transmission,seats,status,image_url,door_count,boot_capacity_liters,cargo_capacity_kg,has_ac,engine_cc,helmet_provided,max_load_kg,body_kind) VALUES
(1,'CAR','CBA-1234','Toyota','Axio',2019,8500,'PETROL','AUTOMATIC',5,'AVAILABLE','/images/car.svg',4,450,NULL,NULL,NULL,NULL,NULL,NULL),
(2,'VAN','CBA-5678','Toyota','KDH Super GL',2020,15000,'DIESEL','MANUAL',14,'RENTED','/images/van.svg',NULL,NULL,900,TRUE,NULL,NULL,NULL,NULL),
(3,'BIKE','WP-BB-0123','Yamaha','FZ V3',2022,3500,'PETROL','MANUAL',2,'AVAILABLE','/images/bike.svg',NULL,NULL,NULL,NULL,149,TRUE,NULL,NULL),
(4,'TRUCK','CBA-9012','Mitsubishi','Canter',2018,18000,'DIESEL','MANUAL',3,'AVAILABLE','/images/truck.svg',NULL,NULL,NULL,NULL,NULL,NULL,3500,'box'),
(5,'CAR','CBA-3456','Nissan','Leaf',2021,9500,'ELECTRIC','AUTOMATIC',5,'AVAILABLE','/images/car.svg',5,435,NULL,NULL,NULL,NULL,NULL,NULL),
(6,'VAN','CBA-7890','Nissan','Caravan',2017,12000,'DIESEL','MANUAL',12,'IN_MAINTENANCE','/images/van.svg',NULL,NULL,700,FALSE,NULL,NULL,NULL,NULL);

INSERT IGNORE INTO bookings (id,user_id,vehicle_id,pickup_date,return_date,pickup_location,return_location,total_amount,status,returned_date,fine) VALUES
(1,2,1,CURDATE() - INTERVAL 20 DAY,CURDATE() - INTERVAL 15 DAY,'Colombo','Colombo',42500,'COMPLETED',CURDATE() - INTERVAL 15 DAY,0),
(2,3,2,CURDATE() - INTERVAL 3 DAY,CURDATE() + INTERVAL 4 DAY,'Kandy','Colombo',105000,'ACTIVE',NULL,0);

INSERT IGNORE INTO payments (id,booking_id,amount,method,payment_date,status,transaction_reference) VALUES
(1,1,42500,'CASH',CURDATE() - INTERVAL 15 DAY,'COMPLETED','TXN-SEED0001'),
(2,2,105000,'ONLINE',CURDATE() - INTERVAL 3 DAY,'PENDING','TXN-SEED0002');

INSERT IGNORE INTO reviews (id,user_id,vehicle_id,rating,comment,review_date,status) VALUES
(1,2,1,5,'Clean car, smooth pickup in Colombo. Highly recommended!',CURDATE() - INTERVAL 14 DAY,'APPROVED'),
(2,3,2,4,'Spacious van, driver seat a bit high but overall great.',CURDATE() - INTERVAL 2 DAY,'APPROVED'),
(3,3,3,5,'Best bike rental in town, helmet was included.',CURDATE() - INTERVAL 1 DAY,'PENDING');
