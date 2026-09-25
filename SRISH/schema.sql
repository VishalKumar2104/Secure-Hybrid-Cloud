CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('Buyer', 'Seller', 'Admin')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_qty INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    size_options VARCHAR(50) DEFAULT 'S, M, L, XL',
    color VARCHAR(30) DEFAULT 'Standard',
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(50) PRIMARY KEY,
    buyer_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Confirmed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    seller_id INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
-- Passwords set to '1234' via BCrypt
INSERT INTO users (name, email, username, password_hash, role) VALUES
('Demo Buyer', 'buyer@srmart.com', 'buyer', '$2a$10$e8p2U3GqJ.C8YvV0YwObeO3Xp3D3X9bK2P8vN8e.4Z3a4b5c6d7e8', 'Buyer'),
('Vogue Fashion Seller', 'seller@srmart.com', 'seller', '$2a$10$e8p2U3GqJ.C8YvV0YwObeO3Xp3D3X9bK2P8vN8e.4Z3a4b5c6d7e8', 'Seller'),
('SR Mart Admin', 'admin@srmart.com', 'admin', '$2a$10$e8p2U3GqJ.C8YvV0YwObeO3Xp3D3X9bK2P8vN8e.4Z3a4b5c6d7e8', 'Admin');

INSERT INTO products (seller_id, name, description, price, stock_qty, category, size_options, color, image_url, status) VALUES
(2, 'Classic Cotton Crewneck Tee', 'Ultra-soft 100% organic breathable cotton shirt.', 799.00, 50, 'Men', 'S, M, L, XL', 'Black', 'https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=800&q=80', 'Active'),
(2, 'Oversized Denim Trucker Jacket', 'Heavyweight vintage washed denim jacket.', 2499.00, 25, 'Outerwear', 'M, L, XL', 'Blue', 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&w=800&q=80', 'Active'),
(2, 'Floral Pleated Silk Midi Dress', 'Elegantly pleated evening dress with flared hem.', 3299.00, 18, 'Women', 'S, M, L', 'Emerald', 'https://images.unsplash.com/photo-1539008835657-9e8e9680c956?auto=format&fit=crop&w=800&q=80', 'Active'),
(2, 'Tailored Slim Chino Trousers', 'Stretch cotton chinos designed for modern smart-casual wear.', 1499.00, 35, 'Men', '30, 32, 34', 'Khaki', 'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=800&q=80', 'Active');