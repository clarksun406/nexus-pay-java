-- Remove legacy secret material and weak bootstrap credentials.

UPDATE api_keys SET plaintext_key = NULL WHERE plaintext_key IS NOT NULL;
ALTER TABLE api_keys DROP COLUMN IF EXISTS plaintext_key;

DELETE FROM user_roles
WHERE user_id IN (
    SELECT id FROM users
    WHERE id = '00000000-0000-0000-0000-000000000001'
      AND email = 'admin@nexuspay.local'
      AND password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
);

DELETE FROM users
WHERE id = '00000000-0000-0000-0000-000000000001'
  AND email = 'admin@nexuspay.local'
  AND password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
