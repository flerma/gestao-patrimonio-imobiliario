--liquibase formatted sql

--changeset fernando:003-create-table-inquilinos
CREATE TABLE inquilinos (
    id UUID PRIMARY KEY,
    tipo_pessoa VARCHAR(20) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    telefone VARCHAR(20),
    data_nascimento DATE,
    observacoes VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT uk_inquilinos_documento UNIQUE (documento)
);
--rollback DROP TABLE inquilinos;
