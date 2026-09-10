--liquibase formatted sql

--changeset fernando:005-add-endereco-to-inquilinos
ALTER TABLE inquilinos
    ADD COLUMN cep VARCHAR(9),
    ADD COLUMN logradouro VARCHAR(255),
    ADD COLUMN numero VARCHAR(20),
    ADD COLUMN complemento VARCHAR(255),
    ADD COLUMN bairro VARCHAR(255),
    ADD COLUMN cidade VARCHAR(255),
    ADD COLUMN estado VARCHAR(2),
    ADD COLUMN pais VARCHAR(100);
--rollback ALTER TABLE inquilinos DROP COLUMN cep, DROP COLUMN logradouro, DROP COLUMN numero, DROP COLUMN complemento, DROP COLUMN bairro, DROP COLUMN cidade, DROP COLUMN estado, DROP COLUMN pais;
