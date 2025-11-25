ALTER TABLE caixas_dagua ADD COLUMN frequencia_atualizacao VARCHAR(255) DEFAULT 'A_CADA_1_HORA';

UPDATE caixas_dagua SET frequencia_atualizacao = 'A_CADA_1_HORA' WHERE frequencia_atualizacao IS NULL;