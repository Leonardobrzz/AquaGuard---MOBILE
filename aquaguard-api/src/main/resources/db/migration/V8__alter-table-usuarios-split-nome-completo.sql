ALTER TABLE usuarios ADD COLUMN nome VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN sobrenome VARCHAR(155);

UPDATE usuarios SET
    nome = SUBSTRING(nome_completo, 1, POSITION(' ' IN nome_completo) - 1),
    sobrenome = SUBSTRING(nome_completo, POSITION(' ' IN nome_completo) + 1);

ALTER TABLE usuarios ALTER COLUMN nome SET NOT NULL;
ALTER TABLE usuarios DROP COLUMN nome_completo;