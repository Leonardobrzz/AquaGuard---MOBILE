ALTER TABLE usuarios ADD COLUMN notificacoes_email_ativas BOOLEAN DEFAULT TRUE;
ALTER TABLE usuarios ADD COLUMN notificacoes_push_ativas BOOLEAN DEFAULT TRUE;


CREATE TABLE dispositivos_usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BINARY(16) NOT NULL,
    push_token VARCHAR(255) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);


ALTER TABLE caixas_dagua ADD COLUMN limite_alerta_percentual INT DEFAULT 20;
ALTER TABLE caixas_dagua ADD COLUMN data_ultimo_alerta_nivel_baixo TIMESTAMP;