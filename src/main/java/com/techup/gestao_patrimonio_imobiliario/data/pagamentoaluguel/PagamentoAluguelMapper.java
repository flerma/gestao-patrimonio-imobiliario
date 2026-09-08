package com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel;

import java.time.YearMonth;

import com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel.PagamentoAluguel;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoMapper;

public final class PagamentoAluguelMapper {

    private PagamentoAluguelMapper() {
    }

    public static PagamentoAluguelEntity toEntity(PagamentoAluguel pagamento, ContratoEntity contratoEntity) {
        if (pagamento == null) {
            return null;
        }
        return PagamentoAluguelEntity.builder()
                .id(pagamento.getId())
                .contrato(contratoEntity)
                .competencia(pagamento.getCompetencia() != null ? pagamento.getCompetencia().atDay(1) : null)
                .dataVencimento(pagamento.getDataVencimento())
                .valorPrevisto(pagamento.getValorPrevisto())
                .valorPago(pagamento.getValorPago())
                .dataPagamento(pagamento.getDataPagamento())
                .status(pagamento.getStatus())
                .formaPagamento(pagamento.getFormaPagamento())
                .observacoes(pagamento.getObservacoes())
                .dataCriacao(pagamento.getDataCriacao())
                .dataAtualizacao(pagamento.getDataAtualizacao())
                .build();
    }

    public static PagamentoAluguel toDomain(PagamentoAluguelEntity entity) {
        if (entity == null) {
            return null;
        }
        return PagamentoAluguel.builder()
                .id(entity.getId())
                .contrato(ContratoMapper.toDomain(entity.getContrato()))
                .competencia(entity.getCompetencia() != null ? YearMonth.from(entity.getCompetencia()) : null)
                .dataVencimento(entity.getDataVencimento())
                .valorPrevisto(entity.getValorPrevisto())
                .valorPago(entity.getValorPago())
                .dataPagamento(entity.getDataPagamento())
                .status(entity.getStatus())
                .formaPagamento(entity.getFormaPagamento())
                .observacoes(entity.getObservacoes())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
