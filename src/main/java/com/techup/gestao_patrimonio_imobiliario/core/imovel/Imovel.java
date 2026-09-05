package com.techup.gestao_patrimonio_imobiliario.core.imovel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;
import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusImovel;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoImovel;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@With
@Builder
@Jacksonized
public class Imovel {

    UUID id;
    Usuario usuario;
    String nome;
    TipoImovel tipo;
    StatusImovel status;
    BigDecimal valorAquisicao;
    BigDecimal valorAtual;
    Endereco endereco;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;
}
