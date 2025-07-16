CREATE TABLE IF NOT EXISTS users (
	user_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	first_name VARCHAR(100),
	last_name VARCHAR(100),
	email VARCHAR(100) NOT NULL UNIQUE,
	avatar_url VARCHAR(200),
	password VARCHAR(100),
	auth_provider VARCHAR(20),
	provider_id VARCHAR(40),
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS personal_expenses (
	expense_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	title VARCHAR(255),
	description TEXT,
	category VARCHAR(100),
	amount DECIMAL(12,2),
	expense_date DATE DEFAULT CURRENT_TIMESTAMP,
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP,
	deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS groups (
	group_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	name VARCHAR(100),
	description TEXT,
	is_active BOOLEAN DEFAULT TRUE,
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS group_members (
	group_member_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id UUID REFERENCES groups(group_id) ON DELETE SET NULL,
	user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	role VARCHAR(20) CHECK (role IN ('Admin','Member')) DEFAULT 'Member',
	joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	UNIQUE(group_id, user_id)
);

CREATE TABLE IF NOT EXISTS group_expenses (
	group_expense_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id UUID REFERENCES groups(group_id) ON DELETE SET NULL,
	added_by UUID REFERENCES users(user_id) ON DELETE SET NULL,
	title VARCHAR(255),
	description TEXT,
	total_amount DECIMAL(12,2),
	expense_date DATE,
	split_method VARCHAR(20) CHECK (split_method IN ('equal','percentage','custom')) DEFAULT 'equal',
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS group_expense_split (
	split_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_expense_id UUID REFERENCES group_expenses(group_expense_id) ON DELETE CASCADE,
	user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	paid_by_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	amount DECIMAL(12,2)
);

CREATE TABLE IF NOT EXISTS group_settlements (
	settlement_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id UUID REFERENCES groups(group_id) ON DELETE SET NULL,
	from_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	to_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	amount DECIMAL(12,2),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS settlement_snapshots (
	snapshot_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id UUID REFERENCES groups(group_id) ON DELETE SET NULL,
	snapshot_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
	notification_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id UUID REFERENCES groups(group_id) ON DELETE SET NULL,
	user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	actor_user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	notification_type VARCHAR(40) CHECK (notification_type IN ('EXPENSE_ADDED', 'EXPENSE_UPDATED', 'EXPENSE_DELETED', 'SETTLEMENT_CREATED', 'SETTLEMENT_PAID', 'MEMBER_ADDED', 'MEMBER_REMOVED')),
	title VARCHAR(255),
	description TEXT,
	seen BOOLEAN DEFAULT FALSE,
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS receipts (
	receipt_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	receipt_name VARCHAR(255),
	vendor_name VARCHAR(255),
	category VARCHAR(100),
	amount DECIMAL(12,2),
	receipt_date DATE DEFAULT CURRENT_TIMESTAMP,
	receipt_file_url TEXT,
	receipt_description TEXT,
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP,
	deleted BOOLEAN DEFAULT FALSE
);


CREATE TABLE IF NOT EXISTS categories (
    category_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE categories
ADD COLUMN logo_url TEXT;


ALTER TABLE personal_expenses
DROP COLUMN IF EXISTS category;

ALTER TABLE personal_expenses
ADD COLUMN category_id UUID REFERENCES categories(category_id) ON DELETE SET NULL;

ALTER TABLE receipts
DROP COLUMN IF EXISTS category;

ALTER TABLE receipts
ADD COLUMN category_id UUID REFERENCES categories(category_id) ON DELETE SET NULL;

INSERT INTO categories (name, description, logo_url) VALUES
('Food', 'Groceries, dining out, takeout, and snacks', 'https://example.com/logos/food.png'),
('Transport', 'Fuel, public transit, rideshare, and travel', 'https://example.com/logos/transport.png'),
('Rent', 'Monthly housing rent or mortgage payments', 'https://example.com/logos/rent.png'),
('Utilities', 'Electricity, water, gas, internet, and mobile bills', 'https://example.com/logos/utilities.png'),
('Entertainment', 'Movies, streaming, concerts, and events', 'https://example.com/logos/entertainment.png'),
('Shopping', 'Clothing, electronics, and other retail purchases', 'https://example.com/logos/shopping.png'),
('Health', 'Medical expenses, insurance, and pharmacy', 'https://example.com/logos/health.png'),
('Education', 'Tuition fees, books, courses, and learning tools', 'https://example.com/logos/education.png'),
('Gifts & Donations', 'Presents, charity, and donations', 'https://example.com/logos/gifts.png');


Select * from categories;

Delete from categories where category_id='92aadfcb-b4a0-4988-b717-8896ee624b52';

Alter table Categories drop column text;
