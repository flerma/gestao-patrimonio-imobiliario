--liquibase formatted sql

--changeset fernando:011-add-role-to-usuarios
-- Duas roles: ADMIN (gerencia a lista de usuarios) e USUARIO (padrao).
-- Usuarios existentes viram USUARIO; autocadastro (tela de login) tambem
-- sempre cria USUARIO - ver AuthService.registrar.
ALTER TABLE usuarios ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USUARIO';
ALTER TABLE usuarios ALTER COLUMN role DROP DEFAULT;
--rollback ALTER TABLE usuarios DROP COLUMN role;
