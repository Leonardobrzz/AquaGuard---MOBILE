ALTER TABLE usuarios ADD COLUMN verification_code VARCHAR(10);

ALTER TABLE usuarios ADD COLUMN verification_code_expires_at TIMESTAMP;

ALTER TABLE usuarios ALTER COLUMN ativo SET DEFAULT FALSE;