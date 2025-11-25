ALTER TABLE caixas_dagua DROP COLUMN id_dispositivo;

ALTER TABLE caixas_dagua ADD COLUMN nome_dispositivo VARCHAR(255);

ALTER TABLE caixas_dagua ADD COLUMN serial_number VARCHAR(255) NOT NULL UNIQUE;

ALTER TABLE caixas_dagua ADD COLUMN chave_api VARCHAR(255) NOT NULL UNIQUE;