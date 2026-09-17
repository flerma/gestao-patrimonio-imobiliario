--liquibase formatted sql

--changeset fernando:010-add-usuario-to-inquilinos
-- Atrela cada inquilino a um usuario (proprietario), para que os dados
-- fiquem isolados por usuario logado. Preenche os registros existentes a
-- partir do proprietario do imovel do contrato mais antigo do inquilino.
ALTER TABLE inquilinos ADD COLUMN usuario_id UUID;

UPDATE inquilinos i
SET usuario_id = (
    SELECT im.usuario_id
    FROM contratos c
    JOIN imoveis im ON im.id = c.imovel_id
    WHERE c.inquilino_id = i.id
    ORDER BY c.data_criacao ASC
    LIMIT 1
)
WHERE i.usuario_id IS NULL;

ALTER TABLE inquilinos ALTER COLUMN usuario_id SET NOT NULL;
ALTER TABLE inquilinos ADD CONSTRAINT fk_inquilinos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id);

-- O documento (CPF/CNPJ) deixa de ser unico globalmente e passa a ser unico
-- por usuario: a mesma pessoa pode ser inquilina de proprietarios diferentes.
ALTER TABLE inquilinos DROP CONSTRAINT uk_inquilinos_documento;
ALTER TABLE inquilinos ADD CONSTRAINT uk_inquilinos_usuario_documento UNIQUE (usuario_id, documento);
--rollback ALTER TABLE inquilinos DROP CONSTRAINT uk_inquilinos_usuario_documento;
--rollback ALTER TABLE inquilinos ADD CONSTRAINT uk_inquilinos_documento UNIQUE (documento);
--rollback ALTER TABLE inquilinos DROP CONSTRAINT fk_inquilinos_usuario;
--rollback ALTER TABLE inquilinos DROP COLUMN usuario_id;
