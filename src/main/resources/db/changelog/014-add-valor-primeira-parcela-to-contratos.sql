--liquibase formatted sql

--changeset fernando:014-add-valor-primeira-parcela-to-contratos
-- Valor previsto da primeira parcela de aluguel do contrato, quando
-- diferente de valor_aluguel. Nulo significa que a primeira parcela usa o
-- mesmo valor das demais (ver PagamentoAluguelService).
ALTER TABLE contratos ADD COLUMN valor_primeira_parcela NUMERIC(15,2);
--rollback ALTER TABLE contratos DROP COLUMN valor_primeira_parcela;
