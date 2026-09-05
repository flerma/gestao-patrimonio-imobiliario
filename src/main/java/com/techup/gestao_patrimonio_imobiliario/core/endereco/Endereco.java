package com.techup.gestao_patrimonio_imobiliario.core.endereco;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder
public class Endereco {

    String cep;
    String logradouro;
    String numero;
    String complemento;
    String bairro;
    String cidade;
    String estado;
    String pais;
}
