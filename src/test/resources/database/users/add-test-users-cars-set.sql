INSERT INTO users (id, email, password, first_name, last_name)
VALUES (5, 'testuser@test.com', 'password', 'customer', 'test');
INSERT INTO users (id, email, password, first_name, last_name)
VALUES (6, 'manager@test.com', 'password', 'manager', 'test');

INSERT INTO roles (id, name) VALUES (2, 'CUSTOMER');
INSERT INTO roles (id, name) VALUES (3, 'MANAGER');


INSERT INTO users_roles (user_id, role_id) VALUES (5, 2);
INSERT INTO users_roles (user_id, role_id) VALUES (6, 3);

INSERT INTO cars (id, brand, model, type, inventory, daily_fee)
VALUES (1, 'Chevrolet', 'Equinox', 'SUV', 10, 50.00),
       (2, 'Ford', 'Focus', 'HATCHBACK', 15, 45.00),
       (3, 'Honda', 'Civic', 'SEDAN', 5, 55.00);

INSERT INTO rentals (id, rental_date, return_date, return_date_actual, car_id, user_id, is_active)
VALUES (1, '2024-03-01', '2024-03-10', '2024-03-09', 1, 5, TRUE),
       (2, '2024-02-01', '2024-02-10', '2024-02-09', 2, 5, TRUE),
       (3, '2024-01-01', '2024-01-05', '2024-01-05', 3, 5, FALSE),
       (4, '2024-02-01', '2024-02-10', '2024-02-09', 2, 6, TRUE),
       (5, '2024-01-01', '2024-01-05', '2024-01-05', 3, 6, FALSE);