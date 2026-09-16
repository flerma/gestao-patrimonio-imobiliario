--liquibase formatted sql

--changeset fernando:009-create-table-refresh-tokens
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    token_hash VARCHAR(255) NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP NOT NULL
);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
--rollback DROP TABLE refresh_tokens;
