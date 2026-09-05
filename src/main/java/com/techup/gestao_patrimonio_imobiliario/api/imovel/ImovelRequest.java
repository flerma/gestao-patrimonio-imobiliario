package com.techup.gestao_patrimonio_imobiliario.api.imovel;

import java.math.BigDecimal;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusImovel;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoImovel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImovelRequest {

    @NotNull
    UUID usuarioId;

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
    @PositiveOrZero
    BigDecimal valorAtual;

    @NotNull
    Endereco endereco;
}
