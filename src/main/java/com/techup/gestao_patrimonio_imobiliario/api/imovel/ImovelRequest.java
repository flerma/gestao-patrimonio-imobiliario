package com.techup.gestao_patrimonio_imobiliario.api.imovel;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusImovel;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoImovel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImovelRequest {

    @NotBlank
    String nome;

    @NotNull
    TipoImovel tipo;

    @NotNull
    StatusImovel status;

    @NotNull
    @PositiveOrZero
    BigDecimal valorAquisicao;

    @NotNull
    @PastOrPresent(message = "A data de aquisicao nao pode ser uma data futura.")
    LocalDate dataAquisicao;

    @NotNull
    @PositiveOrZero
    BigDecimal valorAtual;

    @NotNull
    Endereco endereco;
}
