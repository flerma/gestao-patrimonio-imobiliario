--liquibase formatted sql

--changeset fernando:001-create-table-usuarios
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    provedor_autenticacao VARCHAR(20) NOT NULL,
    id_usuario_provedor VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
--rollback DROP TABLE usuarios;
