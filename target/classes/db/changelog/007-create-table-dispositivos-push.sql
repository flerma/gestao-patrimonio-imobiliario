--liquibase formatted sql

--changeset fernando:007-create-table-dispositivos-push
CREATE TABLE dispositivos_push (
    id UUID PRIMARY KEY,
    expo_push_token VARCHAR(255) NOT NULL,
    usuario_id UUID,
    plataforma VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT uk_dispositivos_push_token UNIQUE (expo_push_token)
);

CREATE INDEX idx_dispositivos_push_usuario ON dispositivos_push (usuario_id);
CREATE INDEX idx_dispositivos_push_ativo ON dispositivos_push (ativo);
--rollback DROP TABLE dispositivos_push;
