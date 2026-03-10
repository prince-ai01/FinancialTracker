-- ============================================
-- Financial Tracker - Database Schema (MySQL)
-- Project: 2nd Year, 4th Semester
-- ============================================

CREATE DATABASE IF NOT EXISTS financial_tracker;
USE financial_tracker;

-- Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    monthly_budget DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories Table
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL,
    type ENUM('income', 'expense') NOT NULL
);

-- Insert default categories
INSERT INTO categories (category_name, type) VALUES
('Salary', 'income'),
('Freelance', 'income'),
('Food', 'expense'),
('Transport', 'expense'),
('Shopping', 'expense'),
('Entertainment', 'expense'),
('Bills', 'expense'),
('Education', 'expense'),
('Health', 'expense'),
('Other', 'expense');

-- Transactions Table
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type ENUM('income', 'expense') NOT NULL,
    description VARCHAR(200),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Sample Data
INSERT INTO users (username, password, email, monthly_budget) VALUES
('prince', 'pass123', 'prince@email.com', 10000.00);

INSERT INTO transactions (user_id, category_id, amount, type, description, transaction_date) VALUES
(1, 1, 15000.00, 'income', 'Monthly Salary', '2025-01-01'),
(1, 3, 2500.00, 'expense', 'Groceries', '2025-01-05'),
(1, 4, 800.00, 'expense', 'Bus Pass', '2025-01-06'),
(1, 5, 3000.00, 'expense', 'Clothes', '2025-01-10'),
(1, 7, 1200.00, 'expense', 'Electricity Bill', '2025-01-15'),
(1, 1, 15000.00, 'income', 'Monthly Salary', '2025-02-01'),
(1, 3, 2200.00, 'expense', 'Groceries', '2025-02-05'),
(1, 6, 500.00, 'expense', 'Movie', '2025-02-12'),
(1, 4, 800.00, 'expense', 'Bus Pass', '2025-02-06'),
(1, 8, 5000.00, 'expense', 'College Fee', '2025-02-20'),
(1, 1, 15000.00, 'income', 'Monthly Salary', '2025-03-01'),
(1, 3, 2800.00, 'expense', 'Groceries', '2025-03-05'),
(1, 9, 1500.00, 'expense', 'Doctor Visit', '2025-03-14'),
(1, 5, 2000.00, 'expense', 'Shoes', '2025-03-18');
