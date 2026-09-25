--liquibase formatted sql

--changeset fernando:015-add-data-aquisicao-to-imoveis
-- Data de aquisicao do imovel. Nula em imoveis cadastrados antes da
-- introducao deste campo.
ALTER TABLE imoveis ADD COLUMN data_aquisicao DATE;
--rollback ALTER TABLE imoveis DROP COLUMN data_aquisicao;
