--liquibase formatted sql

--changeset fernando:017-create-table-notificacoes-enviadas
-- Registro de notificacoes push pontuais ja enviadas, para nao repetir o mesmo
-- aviso todo dia. Ex.: chave "reajuste-<contratoId>-2026-01" garante um unico
-- push de reajuste por contrato por ciclo de reajuste.
CREATE TABLE notificacoes_enviadas (
    id UUID PRIMARY KEY,
    chave VARCHAR(150) NOT NULL,
    usuario_id UUID,
    data_envio TIMESTAMP NOT NULL,
    CONSTRAINT uk_notificacoes_enviadas_chave UNIQUE (chave)
);
--rollback DROP TABLE notificacoes_enviadas;
