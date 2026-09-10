--liquibase formatted sql

--changeset fernando:006-create-table-pagamentos-aluguel
CREATE TABLE pagamentos_aluguel (
    id UUID PRIMARY KEY,
    contrato_id UUID NOT NULL,
    competencia DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    valor_previsto NUMERIC(15,2) NOT NULL,
    valor_pago NUMERIC(15,2),
    data_pagamento DATE,
    status VARCHAR(20) NOT NULL,
    forma_pagamento VARCHAR(20),
    observacoes VARCHAR(1000),
    data_criacao TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_pagamentos_aluguel_contrato FOREIGN KEY (contrato_id) REFERENCES contratos (id),
    CONSTRAINT uk_pagamentos_aluguel_contrato_competencia UNIQUE (contrato_id, competencia),
    CONSTRAINT ck_pagamentos_aluguel_valor_previsto CHECK (valor_previsto >= 0),
    CONSTRAINT ck_pagamentos_aluguel_valor_pago CHECK (valor_pago IS NULL OR valor_pago >= 0)
);

CREATE INDEX idx_pagamentos_aluguel_contrato ON pagamentos_aluguel (contrato_id);
CREATE INDEX idx_pagamentos_aluguel_status ON pagamentos_aluguel (status);
--rollback DROP TABLE pagamentos_aluguel;
