INSERT INTO users (id, email, password, first_name, last_name)
VALUES (1, 'testuser1@example.com', 'password1', 'user', 'first'),
       (2, 'testuser2@example.com', 'password2', 'user', 'second'),
       (3, 'testuser3@example.com', 'password3', 'user', 'third');

INSERT INTO cars (id, brand, model, type, inventory, daily_fee)
VALUES (1, 'Chevrolet', 'Equinox', 'SUV', 10, 50.00),
       (2, 'Ford', 'Focus', 'HATCHBACK', 15, 45.00),
       (3, 'Honda', 'Civic', 'SEDAN', 5, 55.00),
       (4, 'Chevrolet', 'Equinox', 'SUV', 5, 23.08);

INSERT INTO rentals (id, rental_date, return_date, return_date_actual, car_id, user_id, is_active)
VALUES (1, '2023-01-01', '2023-01-10', '2023-01-09', 1, 1, TRUE),
       (2, '2023-02-01', '2023-02-10', '2023-02-09', 2, 1, TRUE),
       (3, '2023-03-01', '2023-03-10', '2023-03-09', 3, 2, FALSE),
       (4, '2023-04-01', '2023-04-10', '2023-04-09', 4, 2, TRUE),
       (5, '2023-04-01', '2023-04-10', '2023-04-09', 4, 3, FALSE);
