package com.techup.gestao_patrimonio_imobiliario.api.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.IndiceReajuste;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoGarantia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ContratoRequest {

    @NotNull
    UUID imovelId;

    @NotNull
    UUID inquilinoId;

    @NotNull
    TipoContrato tipo;

    StatusContrato status;

    @NotNull
    LocalDate dataInicio;

    LocalDate dataFim;

    @NotNull
    @Positive
    BigDecimal valorAluguel;

    @NotNull
    @Min(1)
    @Max(31)
    Integer diaVencimento;

    IndiceReajuste indiceReajuste;

    @PositiveOrZero
    BigDecimal percentualReajuste;

    @Positive
    Integer periodoReajuste;

    TipoGarantia tipoGarantia;

    @PositiveOrZero
    BigDecimal valorGarantia;

    String observacoes;
}
