CREATE TABLE IF NOT EXISTS groups (
	group_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	name VARCHAR(100),
	description TEXT,
	is_active BOOLEAN DEFAULT TRUE,
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS personal_expenses (
	expense_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
	title VARCHAR(255),
	description TEXT,
	category_id UUID References categories(id) ON DELETE SET NULL
	amount DECIMAL(12,2),
	expense_date DATE DEFAULT CURRENT_TIMESTAMP,
	created_at DATE DEFAULT CURRENT_TIMESTAMP,
	updated_at DATE DEFAULT CURRENT_TIMESTAMP,
	deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS settlement_snapshots (
	snapshot_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	group_id UUID REFERENCES groups(group_id) ON DELETE SET NULL,
	snapshot_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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




CREATE TABLE categories (
    category_id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Ownership
    user_id        UUID NOT NULL,                     -- which user owns this category

    -- Display info
    name           TEXT NOT NULL,
    color          TEXT,                              -- optional: HEX code like "#FF9800"

    -- Lifecycle
    is_system_seed BOOLEAN NOT NULL DEFAULT FALSE,    -- true if copied from template at signup
    template_id    UUID REFERENCES category_templates(template_id), -- backref to global template
    is_hidden      BOOLEAN NOT NULL DEFAULT FALSE,    -- user can hide instead of delete
    deleted_at     TIMESTAMPTZ,                       -- soft delete

    -- Audit
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);







CREATE TABLE category_templates (
    template_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Display info
    name          TEXT NOT NULL,
    color         TEXT,            -- optional: HEX or CSS color code

    
    -- Lifecycle flags
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);


INSERT INTO category_templates (name, color)
VALUES
  ('Grocery', '#FF9800'),
  ('Travel', '#2196F3'),
  ('Bills', '#9C27B0'),
  ('Entertainment', '#F44336'),
  ('Health', '#4CAF50');


-- Prevent duplicate category names for the same user (ignoring deleted ones)
CREATE UNIQUE INDEX uq_categories_user_name_active
ON categories(user_id, LOWER(name))
WHERE deleted_at IS NULL;

-- Prevent duplicate seeding of the same template for a user (ignoring deleted ones)
CREATE UNIQUE INDEX uq_categories_user_template_active
ON categories(user_id, template_id)
WHERE deleted_at IS NULL AND template_id IS NOT NULL;


Drop table categories CASCADE ;

truncate table personal_expenses;

SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = current_schema() AND table_type = 'BASE TABLE';

SELECT schemaname, tablename, indexname, indexdef
FROM pg_indexes where schemaname='public'
ORDER BY schemaname, tablename, indexname ;

-- Create function to seed categories after a user is inserted
CREATE OR REPLACE FUNCTION seed_categories_after_user()
RETURNS trigger 
LANGUAGE plpgsql
AS $fn$
BEGIN
  INSERT INTO categories (user_id, name, color, is_system_seed, template_id)
  SELECT 
    NEW.user_id,          -- 👈 use your actual PK column
    ct.name,
    ct.color,
    TRUE,
    ct.template_id
  FROM category_templates ct
  WHERE ct.is_active = TRUE
  ON CONFLICT (user_id, template_id) DO NOTHING;

  RETURN NEW;
END;
$fn$;

ALTER TABLE categories
ADD CONSTRAINT uq_categories_user_template
UNIQUE (user_id, template_id);


CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_user_template
  ON categories (user_id, template_id);



DROP TRIGGER IF EXISTS trg_seed_categories_after_user ON users;

CREATE TRIGGER trg_seed_categories_after_user
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION seed_categories_after_user();



select * from users;

select * from categories;

select * from personal_expenses;

-- Optional DDL fix: missing comma before amount
-- CREATE TABLE IF NOT EXISTS personal_expenses (
--   expense_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
--   user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
--   title VARCHAR(255),
--   description TEXT,
--   category_id UUID REFERENCES categories(id) ON DELETE SET NULL,  -- <—
--   amount DECIMAL(12,2),
--   expense_date DATE DEFAULT CURRENT_TIMESTAMP,
--   created_at DATE DEFAULT CURRENT_TIMESTAMP,
--   updated_at DATE DEFAULT CURRENT_TIMESTAMP,
--   deleted BOOLEAN DEFAULT FALSE
-- );

BEGIN;

INSERT INTO personal_expenses
  (user_id, title, description, category_id, amount, expense_date, deleted)
VALUES

-- ── September 2025 ──────────────────────────────────────────
('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Morning Coffee',    'Latte before work',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 4.50,   '2025-09-10', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'No Frills Grocery', 'Weekly produce, milk, eggs',
 '9db88be3-460c-4802-a0e6-57500444a1e4', 85.20,  '2025-09-09', FALSE), -- Grocery

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Thai Express Dinner','Pad thai + drink',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 22.99,  '2025-09-08', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Transit Tickets',   'Two bus rides',
 'c2deac5b-6de2-40fe-b89c-5066641e885e', 3.40,   '2025-09-07', FALSE), -- Travel

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Shell Gas',         'Full tank',
 'c2deac5b-6de2-40fe-b89c-5066641e885e', 58.76,  '2025-09-06', FALSE), -- Travel

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Hydro One Bill',    'Electricity – Aug cycle',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 121.50, '2025-09-05', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Home Internet',     'Monthly plan',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 70.00,  '2025-09-03', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Mobile Plan',       'Monthly plan',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 55.00,  '2025-09-02', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'September Rent',    'Apartment rent',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 950.00, '2025-09-01', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Netflix',           'Monthly subscription',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 14.99,  '2025-09-01', FALSE), -- Bills

-- ── August 2025 ─────────────────────────────────────────────
('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Costco Grocery',    'Staples & snacks',
 '9db88be3-460c-4802-a0e6-57500444a1e4', 96.73,  '2025-08-29', FALSE), -- Grocery

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'IMAX Movie',        'Evening show',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 27.50,  '2025-08-27', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Clinic Visit',      'General checkup',
 '0e7c72aa-eeb7-4a91-a9a5-41b26ed9a78a', 35.00,  '2025-08-25', FALSE), -- Health

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Uber to Downtown',  'Evening ride',
 'c2deac5b-6de2-40fe-b89c-5066641e885e', 12.80,  '2025-08-22', FALSE), -- Travel

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Amazon Purchase',   'USB-C hub',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 21.49,  '2025-08-18', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Cafe Stop',         'Latte + croissant',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 7.25,   '2025-08-15', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Hydro One Bill',    'Electricity – Jul cycle',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 118.20, '2025-08-12', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Spotify',           'Monthly subscription',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 10.99,  '2025-08-10', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Gym Membership',    'Monthly fee',
 '0e7c72aa-eeb7-4a91-a9a5-41b26ed9a78a', 39.99,  '2025-08-06', FALSE), -- Health

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'August Rent',       'Apartment rent',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 950.00, '2025-08-01', FALSE), -- Bills

-- ── July 2025 ───────────────────────────────────────────────
('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Metro Grocery',     'Weekly essentials',
 '9db88be3-460c-4802-a0e6-57500444a1e4', 75.31,  '2025-07-28', FALSE), -- Grocery

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Road Trip Gas',     'Weekend drive',
 'c2deac5b-6de2-40fe-b89c-5066641e885e', 62.44,  '2025-07-26', FALSE), -- Travel

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Provincial Park Pass','Day pass + parking',
 'c2deac5b-6de2-40fe-b89c-5066641e885e', 18.00,  '2025-07-22', FALSE), -- Travel

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Shoppers Drug Mart','Vitamins & toiletries',
 '0e7c72aa-eeb7-4a91-a9a5-41b26ed9a78a', 22.67,  '2025-07-19', FALSE), -- Health

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Hydro One Bill',    'Electricity – Jun cycle',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 115.90, '2025-07-15', FALSE), -- Bills

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Birthday Gift',     'Gift for friend',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 30.00,  '2025-07-12', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'Uniqlo T-Shirt',    'Summer clothing',
 '0cb658ce-ca8a-4a6d-a254-b4cf992081a7', 45.00,  '2025-07-05', FALSE), -- Entertainment

('bf257b84-b398-4fbd-8d62-cbf9e3a9ba38', 'July Rent',         'Apartment rent',
 '2a69d5a7-5f3a-4718-bd84-ecc1f18f2b30', 950.00, '2025-07-01', FALSE); -- Bills


 Select * from users;


Select * from categories;


Select * from personal_expenses;

Truncate table personal_expenses;

TRUNCATE TABLE categories CASCADE;

Select * from personal_expenses where user_id='bf257b84-b398-4fbd-8d62-cbf9e3a9ba38';

Select * from users;

Select * from categories;


Select sum(p.amount) from personal_expenses p where p.user_id = 'bf257b84-b398-4fbd-8d62-cbf9e3a9ba38' and p.deleted = false and p.expense_date='2026-02-11';

Select sum(p.amount) from personal_expenses p where p.user_id = 'bf257b84-b398-4fbd-8d62-cbf9e3a9ba38' and p.deleted = false and p.expense_date between '2025-08-01' and '2025-08-31';

Select count(*) from personal_expenses p where p.user_id = 'bf257b84-b398-4fbd-8d62-cbf9e3a9ba38' and p.deleted = false and p.expense_date between '2025-08-01' and '2025-08-31';

Select c.name, count(*) from personal_expenses p join categories c on p.category_id = c.category_id    where p.user_id = 'bf257b84-b398-4fbd-8d62-cbf9e3a9ba38' and p.deleted = false and p.expense_date between '2025-08-01' and '2025-08-31' group by c.name;



SELECT c.name, COUNT(*) as transaction_count
FROM personal_expenses p
JOIN categories c ON p.category_id = c.category_id
WHERE p.user_id = 'bf257b84-b398-4fbd-8d62-cbf9e3a9ba38'
AND p.deleted = false
AND p.expense_date BETWEEN '2025-08-01' AND '2025-08-31'
GROUP BY c.name
ORDER BY transaction_count DESC