package com.techup.gestao_patrimonio_imobiliario.data.ibge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Resposta (parcial) da API de municípios do IBGE. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MunicipioIbgeResponse {

    private Long id;
    private String nome;
}
