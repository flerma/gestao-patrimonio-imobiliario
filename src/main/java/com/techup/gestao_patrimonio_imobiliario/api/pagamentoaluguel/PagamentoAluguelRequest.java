package com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.FormaPagamento;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusPagamentoAluguel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PagamentoAluguelRequest {

    @NotNull
    UUID contratoId;

    @NotNull
    YearMonth competencia;

    @NotNull
    LocalDate dataVencimento;

    @NotNull
    @Positive
    BigDecimal valorPrevisto;

    @PositiveOrZero
    BigDecimal valorPago;

    LocalDate dataPagamento;

    StatusPagamentoAluguel status;

    FormaPagamento formaPagamento;

    String observacoes;
}
