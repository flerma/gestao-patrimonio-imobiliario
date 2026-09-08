package com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.FormaPagamento;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusPagamentoAluguel;
import com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel.PagamentoAluguel;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PagamentoAluguelResponse {

    UUID id;
    UUID contratoId;
    YearMonth competencia;
    LocalDate dataVencimento;
    BigDecimal valorPrevisto;
    BigDecimal valorPago;
    LocalDate dataPagamento;
    /** Status persistido. */
    StatusPagamentoAluguel status;
    /** Status "real" na data de hoje (PENDENTE vencido vira EM_ATRASO). */
    StatusPagamentoAluguel statusEfetivo;
    boolean emAtraso;
    /** valorPrevisto - valorPago (positivo => ainda ha valor em aberto). */
    BigDecimal saldo;
    FormaPagamento formaPagamento;
    String observacoes;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;

    public static PagamentoAluguelResponse from(PagamentoAluguel pagamento) {
        if (pagamento == null) {
            return null;
        }
        LocalDate hoje = LocalDate.now();
        return PagamentoAluguelResponse.builder()
                .id(pagamento.getId())
                .contratoId(pagamento.getContrato() != null ? pagamento.getContrato().getId() : null)
                .competencia(pagamento.getCompetencia())
                .dataVencimento(pagamento.getDataVencimento())
                .valorPrevisto(pagamento.getValorPrevisto())
                .valorPago(pagamento.getValorPago())
                .dataPagamento(pagamento.getDataPagamento())
                .status(pagamento.getStatus())
                .statusEfetivo(pagamento.statusEfetivo(hoje))
                .emAtraso(pagamento.estaEmAtraso(hoje))
                .saldo(pagamento.calcularSaldo())
                .formaPagamento(pagamento.getFormaPagamento())
                .observacoes(pagamento.getObservacoes())
                .dataCriacao(pagamento.getDataCriacao())
                .dataAtualizacao(pagamento.getDataAtualizacao())
                .build();
    }
}
