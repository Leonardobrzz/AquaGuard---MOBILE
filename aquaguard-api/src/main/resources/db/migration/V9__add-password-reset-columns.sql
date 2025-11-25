-- Adicionar colunas para reset de senha
ALTER TABLE usuarios ADD COLUMN password_reset_token VARCHAR(10);
ALTER TABLE usuarios ADD COLUMN password_reset_token_expires_at TIMESTAMP;
