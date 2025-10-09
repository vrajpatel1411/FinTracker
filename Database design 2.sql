select * from personal_expenses;


Alter table personal_expenses drop category_category_id;
select * from users;


selec


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


 CREATE TABLE categories (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),

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

-- 🔒 Constraints for idempotency and data integrity

-- Prevent duplicate category names for the same user (ignoring deleted ones)
CREATE UNIQUE INDEX uq_categories_user_name_active
ON categories(user_id, LOWER(name))
WHERE deleted_at IS NULL;

-- Prevent duplicate seeding of the same template for a user (ignoring deleted ones)
CREATE UNIQUE INDEX uq_categories_user_template_active
ON categories(user_id, template_id)
WHERE deleted_at IS NULL AND template_id IS NOT NULL;



