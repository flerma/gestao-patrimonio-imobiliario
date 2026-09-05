package com.techup.gestao_patrimonio_imobiliario.data.contrato;

import com.techup.gestao_patrimonio_imobiliario.core.contrato.Contrato;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelEntity;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelMapper;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoMapper;

public final class ContratoMapper {

    private ContratoMapper() {
    }

    public static ContratoEntity toEntity(Contrato contrato, ImovelEntity imovelEntity, InquilinoEntity inquilinoEntity) {
        if (contrato == null) {
            return null;
        }
        return ContratoEntity.builder()
                .id(contrato.getId())
                .imovel(imovelEntity)
                .inquilino(inquilinoEntity)
                .tipo(contrato.getTipo())
                .status(contrato.getStatus())
                .dataInicio(contrato.getDataInicio())
                .dataFim(contrato.getDataFim())
                .valorAluguel(contrato.getValorAluguel())
                .diaVencimento(contrato.getDiaVencimento())
                .indiceReajuste(contrato.getIndiceReajuste())
                .percentualReajuste(contrato.getPercentualReajuste())
                .periodoReajuste(contrato.getPeriodoReajuste())
                .tipoGarantia(contrato.getTipoGarantia())
                .valorGarantia(contrato.getValorGarantia())
                .observacoes(contrato.getObservacoes())
                .dataCriacao(contrato.getDataCriacao())
                .dataAtualizacao(contrato.getDataAtualizacao())
                .build();
    }

    public static Contrato toDomain(ContratoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Contrato.builder()
                .id(entity.getId())
                .imovel(ImovelMapper.toDomain(entity.getImovel()))
                .inquilino(InquilinoMapper.toDomain(entity.getInquilino()))
                .tipo(entity.getTipo())
                .status(entity.getStatus())
                .dataInicio(entity.getDataInicio())
                .dataFim(entity.getDataFim())
                .valorAluguel(entity.getValorAluguel())
                .diaVencimento(entity.getDiaVencimento())
                .indiceReajuste(entity.getIndiceReajuste())
                .percentualReajuste(entity.getPercentualReajuste())
                .periodoReajuste(entity.getPeriodoReajuste())
                .tipoGarantia(entity.getTipoGarantia())
                .valorGarantia(entity.getValorGarantia())
                .observacoes(entity.getObservacoes())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
