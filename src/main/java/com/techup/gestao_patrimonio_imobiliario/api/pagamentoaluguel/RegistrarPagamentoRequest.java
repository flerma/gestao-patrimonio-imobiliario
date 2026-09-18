package com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel;

import java.time.LocalDate;

import com.techup.gestao_patrimonio_imobiliario.core.enums.FormaPagamento;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

/**
 * Registra a quitacao integral de uma cobranca: o valor pago e sempre
 * {@code valorPrevisto} e o status resultante e sempre PAGO (ver
 * {@link com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel.PagamentoAluguelService#registrarPagamento}).
 */
@Value
@Builder
public class RegistrarPagamentoRequest {

    @NotNull
    LocalDate dataPagamento;

    @NotNull
    FormaPagamento formaPagamento;

    String observacoes;
}
