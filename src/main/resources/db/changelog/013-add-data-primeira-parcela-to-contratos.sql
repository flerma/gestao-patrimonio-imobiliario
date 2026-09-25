--liquibase formatted sql

--changeset fernando:013-add-data-primeira-parcela-to-contratos
-- Data de vencimento da primeira parcela de aluguel do contrato. Nula em
-- contratos existentes (criados antes deste campo) - a geracao de parcelas
-- cai no comportamento legado para eles (ver PagamentoAluguelService).
ALTER TABLE contratos ADD COLUMN data_primeira_parcela DATE;
--rollback ALTER TABLE contratos DROP COLUMN data_primeira_parcela;
