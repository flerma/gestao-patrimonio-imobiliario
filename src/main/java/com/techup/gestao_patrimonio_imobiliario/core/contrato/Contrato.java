package com.techup.gestao_patrimonio_imobiliario.core.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.IndiceReajuste;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoGarantia;
import com.techup.gestao_patrimonio_imobiliario.core.imovel.Imovel;
import com.techup.gestao_patrimonio_imobiliario.core.inquilino.Inquilino;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@With
@Builder
@Jacksonized
public class Contrato {

    UUID id;
    Imovel imovel;
    Inquilino inquilino;
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
}
