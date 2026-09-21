--liquibase formatted sql

--changeset fernando:012-seed-usuario-admin
-- Usuario ADMIN inicial da aplicacao (login = "admin_gestao", senha = "Fml7544$").
-- Hash BCrypt abaixo, gerado com o mesmo PasswordEncoder da aplicacao.
INSERT INTO usuarios (
    id, nome, email, senha, telefone, provedor_autenticacao,
    id_usuario_provedor, status, role, data_criacao, data_atualizacao
) VALUES (
    'e8e0daa4-2b06-4a18-bfa2-049d07d0c967',
    'Administrador',
    'admin_gestao',
    '$2a$10$EJGn2VbS0wOcUWSmihE3RuNveyTENC3/5MO2MAtnEu4LpyMtsXgxi',
    NULL,
    'LOCAL',
    NULL,
    'ATIVO',
    'ADMIN',
    now(),
    now()
);
--rollback DELETE FROM usuarios WHERE email = 'admin_gestao';
