package com.techup.gestao_patrimonio_imobiliario.api.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.api.imovel.ImovelResponse;
import com.techup.gestao_patrimonio_imobiliario.api.inquilino.InquilinoResponse;
import com.techup.gestao_patrimonio_imobiliario.core.contrato.Contrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.IndiceReajuste;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoGarantia;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ContratoResponse {

    UUID id;
    ImovelResponse imovel;
    InquilinoResponse inquilino;
    TipoContrato tipo;
    StatusContrato status;
    LocalDate dataInicio;
    LocalDate dataFim;
    BigDecimal valorAluguel;
    Integer diaVencimento;
    IndiceReajuste indiceReajuste;
    BigDecimal percentualReajuste;
    Integer periodoReajuste;
    TipoGarantia tipoGarantia;
    BigDecimal valorGarantia;
    String observacoes;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;

    public static ContratoResponse from(Contrato contrato) {
        if (contrato == null) {
            return null;
        }
        return ContratoResponse.builder()
                .id(contrato.getId())
                .imovel(ImovelResponse.from(contrato.getImovel()))
                .inquilino(InquilinoResponse.from(contrato.getInquilino()))
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
}
