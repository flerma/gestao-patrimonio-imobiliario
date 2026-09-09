package com.techup.gestao_patrimonio_imobiliario.api.alerta;

import com.techup.gestao_patrimonio_imobiliario.core.alerta.Alerta;
import com.techup.gestao_patrimonio_imobiliario.core.alerta.SeveridadeAlerta;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AlertaResponse {

    String id;
    SeveridadeAlerta severidade;
    String titulo;
    String descricao;

    public static AlertaResponse from(Alerta alerta) {
        if (alerta == null) {
            return null;
        }
        return AlertaResponse.builder()
                .id(alerta.id())
                .severidade(alerta.severidade())
                .titulo(alerta.titulo())
                .descricao(alerta.descricao())
                .build();
    }
}
