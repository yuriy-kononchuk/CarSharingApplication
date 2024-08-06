INSERT INTO payments (id, payment_status, payment_type, rental_id, session_url, session_id, amount)
VALUES (1, 'PAID', 'PAYMENT', 1, 'http://example.com/session/123', 'session123', 100.00),
       (2, 'PENDING', 'FINE', 2, 'http://example.com/session/456', 'session456', 50.00),
       (3, 'PAID', 'PAYMENT', 4, 'http://example.com/session/777', 'session777', 100.00),
       (4, 'PENDING', 'FINE', 5, 'http://example.com/session/888', 'session888', 50.00);

