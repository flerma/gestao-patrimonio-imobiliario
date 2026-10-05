--liquibase formatted sql

--changeset fernando:016-create-table-codigos-redefinicao-senha
-- Codigos de verificacao do "Esqueceu a sua senha?". Guarda so o hash do
-- codigo (nunca o codigo em si), validade de 30 minutos e tentativas erradas.
-- Ao reenviar, os codigos anteriores do usuario sao invalidados (usado = true).
CREATE TABLE codigos_redefinicao_senha (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    codigo_hash VARCHAR(255) NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    tentativas INTEGER NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL
);
CREATE INDEX idx_codigos_redefinicao_senha_usuario ON codigos_redefinicao_senha (usuario_id);
--rollback DROP TABLE codigos_redefinicao_senha;
