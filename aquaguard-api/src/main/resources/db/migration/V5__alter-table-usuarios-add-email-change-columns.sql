ALTER TABLE usuarios ADD COLUMN pending_email VARCHAR(255);

ALTER TABLE usuarios ADD COLUMN email_change_code VARCHAR(10);

ALTER TABLE usuarios ADD COLUMN email_change_code_expires_at TIMESTAMP;