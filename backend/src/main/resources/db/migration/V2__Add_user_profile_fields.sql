ALTER TABLE app_user
  ADD COLUMN first_name varchar(100),
  ADD COLUMN last_name varchar(100),
  ADD COLUMN address varchar(255),
  ADD COLUMN city varchar(100),
  ADD COLUMN state varchar(2),
  ADD COLUMN phone varchar(30);

-- Backfill existing rows so NOT NULL can be applied safely
UPDATE app_user
SET
  first_name = COALESCE(first_name, 'Unknown'),
  last_name  = COALESCE(last_name,  'Unknown'),
  city       = COALESCE(city,       'Unknown'),
  state      = COALESCE(state,      'CA'),
  phone      = COALESCE(phone,      '000-000-0000');

ALTER TABLE app_user
  ALTER COLUMN first_name SET NOT NULL,
  ALTER COLUMN last_name  SET NOT NULL,
  ALTER COLUMN city       SET NOT NULL,
  ALTER COLUMN state      SET NOT NULL,
  ALTER COLUMN phone      SET NOT NULL;

-- Optional: basic sanity check for state length
ALTER TABLE app_user
  ADD CONSTRAINT ck_app_user_state_len CHECK (char_length(state) = 2);