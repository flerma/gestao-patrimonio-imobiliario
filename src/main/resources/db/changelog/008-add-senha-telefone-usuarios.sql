--liquibase formatted sql

--changeset fernando:008-add-senha-telefone-usuarios
ALTER TABLE usuarios ADD COLUMN senha VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN telefone VARCHAR(30);
ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_telefone UNIQUE (telefone);
--rollback ALTER TABLE usuarios DROP CONSTRAINT uk_usuarios_telefone;
--rollback ALTER TABLE usuarios DROP COLUMN telefone;
--rollback ALTER TABLE usuarios DROP COLUMN senha;
