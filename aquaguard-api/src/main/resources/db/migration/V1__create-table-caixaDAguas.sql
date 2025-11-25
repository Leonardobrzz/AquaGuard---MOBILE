CREATE TABLE usuarios (
    id BINARY(16) NOT NULL,
    nome_completo VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE caixas_dagua (
    id BINARY(16) NOT NULL,
    usuario_id BINARY(16) NOT NULL,
    id_dispositivo VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    capacidade DECIMAL(10, 2) NOT NULL,
    meta_diaria DECIMAL(10, 2),
    meta_semanal DECIMAL(10, 2),
    meta_mensal DECIMAL(10, 2),
    criado_em TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE leituras_volume (
    id BIGINT NOT NULL AUTO_INCREMENT,
    caixa_dagua_id BINARY(16) NOT NULL,
    volume_litros DECIMAL(10, 2) NOT NULL,
    data_hora_leitura TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (caixa_dagua_id) REFERENCES caixas_dagua(id)
);
