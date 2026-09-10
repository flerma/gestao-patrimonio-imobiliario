--liquibase formatted sql

--changeset fernando:004-create-table-contratos
CREATE TABLE contratos (
    id UUID PRIMARY KEY,
    imovel_id UUID NOT NULL,
    inquilino_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    valor_aluguel NUMERIC(15,2) NOT NULL,
    dia_vencimento INTEGER NOT NULL,
    indice_reajuste VARCHAR(20),
    percentual_reajuste NUMERIC(7,4),
    periodo_reajuste INTEGER,
    tipo_garantia VARCHAR(30),
    valor_garantia NUMERIC(15,2),
    observacoes VARCHAR(1000),
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_contratos_imovel FOREIGN KEY (imovel_id) REFERENCES imoveis (id),
    CONSTRAINT fk_contratos_inquilino FOREIGN KEY (inquilino_id) REFERENCES inquilinos (id),
    CONSTRAINT ck_contratos_dia_vencimento CHECK (dia_vencimento BETWEEN 1 AND 31)
);
--rollback DROP TABLE contratos;
