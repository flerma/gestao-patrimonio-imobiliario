package com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.contrato.Contrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.FormaPagamento;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusPagamentoAluguel;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@With
@Builder
@Jacksonized
public class PagamentoAluguel {

    UUID id;
    Contrato contrato;
    YearMonth competencia;
    LocalDate dataVencimento;
    BigDecimal valorPrevisto;
    BigDecimal valorPago;
    LocalDate dataPagamento;
    StatusPagamentoAluguel status;
    FormaPagamento formaPagamento;
    String observacoes;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;

    /**
     * Diferenca entre o que deveria ser pago e o que efetivamente entrou.
     * Positivo => ainda ha valor a receber; zero ou negativo => quitado.
     */
    public BigDecimal calcularSaldo() {
        BigDecimal pago = valorPago != null ? valorPago : BigDecimal.ZERO;
        return valorPrevisto.subtract(pago);
    }

    public boolean estaQuitado() {
        return calcularSaldo().signum() <= 0;
    }

    /**
     * Regra de dominio: um pagamento esta em atraso quando ainda ha saldo em aberto
     * (nao pago ou pago parcialmente) e a data de vencimento ja passou. Nao depende
     * apenas do status armazenado.
     */
    public boolean estaEmAtraso(LocalDate dataAtual) {
        if (dataAtual == null || dataVencimento == null) {
            return false;
        }
        boolean emAberto = status == StatusPagamentoAluguel.PENDENTE
                || status == StatusPagamentoAluguel.PAGO_PARCIALMENTE
                || status == StatusPagamentoAluguel.EM_ATRASO;
        return emAberto && dataVencimento.isBefore(dataAtual);
    }

    /**
     * Status "real" considerando a data atual: converte PENDENTE/PAGO_PARCIALMENTE
     * vencidos em EM_ATRASO sem precisar de rotina agendada.
     */
    public StatusPagamentoAluguel statusEfetivo(LocalDate dataAtual) {
        return estaEmAtraso(dataAtual) ? StatusPagamentoAluguel.EM_ATRASO : status;
    }

    /**
     * Determina o status apos o registro de um pagamento:
     * - saldo em aberto  => PAGO_PARCIALMENTE
     * - quitado no prazo  => PAGO
     * - quitado com atraso => PAGO_COM_ATRASO
     */
    public StatusPagamentoAluguel resolverStatusAposPagamento() {
        if (valorPago == null || dataPagamento == null) {
            return status;
        }
        if (!estaQuitado()) {
            return StatusPagamentoAluguel.PAGO_PARCIALMENTE;
        }
        return dataPagamento.isAfter(dataVencimento)
                ? StatusPagamentoAluguel.PAGO_COM_ATRASO
                : StatusPagamentoAluguel.PAGO;
    }
}
