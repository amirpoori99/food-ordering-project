-- Test data for Analytics tests
-- This file is imported by Hibernate during test setup

-- Insert test users
INSERT INTO users (id, username, email, phone, password_hash, is_active, created_at, updated_at) VALUES (1, 'testuser1', 'user1@test.com', '+12345678901', 'password123', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO users (id, username, email, phone, password_hash, is_active, created_at, updated_at) VALUES (2, 'testuser2', 'user2@test.com', '+12345678902', 'password123', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO users (id, username, email, phone, password_hash, is_active, created_at, updated_at) VALUES (3, 'testuser3', 'user3@test.com', '+12345678903', 'password123', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- Insert test restaurants
INSERT INTO restaurants (id, name, address, phone, cuisine_type, rating, is_active, created_at, updated_at) VALUES (1, 'Test Restaurant 1', '123 Test St', '+12345678910', 'Italian', 4.5, 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO restaurants (id, name, address, phone, cuisine_type, rating, is_active, created_at, updated_at) VALUES (2, 'Test Restaurant 2', '456 Test Ave', '+12345678911', 'Chinese', 4.2, 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO restaurants (id, name, address, phone, cuisine_type, rating, is_active, created_at, updated_at) VALUES (3, 'Test Restaurant 3', '789 Test Blvd', '+12345678912', 'Mexican', 4.0, 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- Insert test food items
INSERT INTO food_items (id, restaurant_id, name, description, price, category, is_available, created_at, updated_at) VALUES (1, 1, 'Pizza Margherita', 'Classic Italian pizza', 15.00, 'Main Course', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO food_items (id, restaurant_id, name, description, price, category, is_available, created_at, updated_at) VALUES (2, 1, 'Pasta Carbonara', 'Creamy pasta dish', 12.00, 'Main Course', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO food_items (id, restaurant_id, name, description, price, category, is_available, created_at, updated_at) VALUES (3, 2, 'Kung Pao Chicken', 'Spicy Chinese dish', 18.00, 'Main Course', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');
INSERT INTO food_items (id, restaurant_id, name, description, price, category, is_available, created_at, updated_at) VALUES (4, 3, 'Tacos', 'Mexican street food', 8.00, 'Main Course', 1, '2024-01-01 10:00:00', '2024-01-01 10:00:00');

-- Insert test orders with analytics data
INSERT INTO orders (id, user_id, restaurant_id, total_amount, status, order_date, created_at, updated_at) VALUES (1, 1, 1, 1000.00, 'COMPLETED', '2024-01-15 12:00:00', '2024-01-15 12:00:00', '2024-01-15 12:00:00');
INSERT INTO orders (id, user_id, restaurant_id, total_amount, status, order_date, created_at, updated_at) VALUES (2, 2, 1, 500.00, 'COMPLETED', '2024-01-16 12:00:00', '2024-01-16 12:00:00', '2024-01-16 12:00:00');
INSERT INTO orders (id, user_id, restaurant_id, total_amount, status, order_date, created_at, updated_at) VALUES (3, 3, 2, 750.00, 'COMPLETED', '2024-01-17 12:00:00', '2024-01-17 12:00:00', '2024-01-17 12:00:00');
INSERT INTO orders (id, user_id, restaurant_id, total_amount, status, order_date, created_at, updated_at) VALUES (4, 1, 3, 300.00, 'COMPLETED', '2024-01-18 12:00:00', '2024-01-18 12:00:00', '2024-01-18 12:00:00');
INSERT INTO orders (id, user_id, restaurant_id, total_amount, status, order_date, created_at, updated_at) VALUES (5, 2, 1, 600.00, 'COMPLETED', '2024-01-19 12:00:00', '2024-01-19 12:00:00', '2024-01-19 12:00:00');

-- Insert test order items
INSERT INTO order_items (id, order_id, food_item_id, quantity, price, created_at, updated_at) VALUES (1, 1, 1, 2, 15.00, '2024-01-15 12:00:00', '2024-01-15 12:00:00');
INSERT INTO order_items (id, order_id, food_item_id, quantity, price, created_at, updated_at) VALUES (2, 1, 2, 1, 12.00, '2024-01-15 12:00:00', '2024-01-15 12:00:00');
INSERT INTO order_items (id, order_id, food_item_id, quantity, price, created_at, updated_at) VALUES (3, 2, 1, 1, 15.00, '2024-01-16 12:00:00', '2024-01-16 12:00:00');
INSERT INTO order_items (id, order_id, food_item_id, quantity, price, created_at, updated_at) VALUES (4, 3, 3, 2, 18.00, '2024-01-17 12:00:00', '2024-01-17 12:00:00');
INSERT INTO order_items (id, order_id, food_item_id, quantity, price, created_at, updated_at) VALUES (5, 4, 4, 3, 8.00, '2024-01-18 12:00:00', '2024-01-18 12:00:00');
INSERT INTO order_items (id, order_id, food_item_id, quantity, price, created_at, updated_at) VALUES (6, 5, 1, 1, 15.00, '2024-01-19 12:00:00', '2024-01-19 12:00:00');

-- Insert test ratings
INSERT INTO ratings (id, user_id, restaurant_id, rating, comment, created_at, updated_at) VALUES (1, 1, 1, 5, 'Great food!', '2024-01-15 13:00:00', '2024-01-15 13:00:00');
INSERT INTO ratings (id, user_id, restaurant_id, rating, comment, created_at, updated_at) VALUES (2, 2, 1, 4, 'Good service', '2024-01-16 13:00:00', '2024-01-16 13:00:00');
INSERT INTO ratings (id, user_id, restaurant_id, rating, comment, created_at, updated_at) VALUES (3, 3, 2, 4, 'Nice atmosphere', '2024-01-17 13:00:00', '2024-01-17 13:00:00');

-- Insert test transactions
INSERT INTO transactions (id, order_id, amount, payment_method, status, transaction_date, created_at, updated_at) VALUES (1, 1, 1000.00, 'CREDIT_CARD', 'COMPLETED', '2024-01-15 12:05:00', '2024-01-15 12:05:00', '2024-01-15 12:05:00');
INSERT INTO transactions (id, order_id, amount, payment_method, status, transaction_date, created_at, updated_at) VALUES (2, 2, 500.00, 'CASH', 'COMPLETED', '2024-01-16 12:05:00', '2024-01-16 12:05:00', '2024-01-16 12:05:00');
INSERT INTO transactions (id, order_id, amount, payment_method, status, transaction_date, created_at, updated_at) VALUES (3, 3, 750.00, 'CREDIT_CARD', 'COMPLETED', '2024-01-17 12:05:00', '2024-01-17 12:05:00', '2024-01-17 12:05:00');
INSERT INTO transactions (id, order_id, amount, payment_method, status, transaction_date, created_at, updated_at) VALUES (4, 4, 300.00, 'CASH', 'COMPLETED', '2024-01-18 12:05:00', '2024-01-18 12:05:00', '2024-01-18 12:05:00');
INSERT INTO transactions (id, order_id, amount, payment_method, status, transaction_date, created_at, updated_at) VALUES (5, 5, 600.00, 'CREDIT_CARD', 'COMPLETED', '2024-01-19 12:05:00', '2024-01-19 12:05:00', '2024-01-19 12:05:00'); 