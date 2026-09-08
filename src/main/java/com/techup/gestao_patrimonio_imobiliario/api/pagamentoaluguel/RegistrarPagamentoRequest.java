package com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.techup.gestao_patrimonio_imobiliario.core.enums.FormaPagamento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistrarPagamentoRequest {

    @NotNull
    @PositiveOrZero
    BigDecimal valorPago;

    /** Se nao informada, assume a data atual. */
    LocalDate dataPagamento;

    FormaPagamento formaPagamento;

    String observacoes;
}
