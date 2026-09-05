package com.techup.gestao_patrimonio_imobiliario.integration.viacep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resposta do ViaCEP. Quando o CEP não existe, o serviço responde
 * {@code { "erro": "true" }} com HTTP 200.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViaCepResponse {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    /** Município. */
    private String localidade;
    /** Sigla da unidade federativa (ex.: SP). */
    private String uf;
    /** Presente (e "true") apenas quando o CEP não é encontrado. */
    private Boolean erro;

    public boolean isErro() {
        return Boolean.TRUE.equals(erro);
    }
}
