package com.techup.gestao_patrimonio_imobiliario.api.notificacao;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.data.notificacao.DispositivoPushEntity;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DispositivoPushResponse {

    UUID id;
    String expoPushToken;
    UUID usuarioId;
    String plataforma;
    boolean ativo;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;

    public static DispositivoPushResponse from(DispositivoPushEntity entity) {
        if (entity == null) {
            return null;
        }
        return DispositivoPushResponse.builder()
                .id(entity.getId())
                .expoPushToken(entity.getExpoPushToken())
                .usuarioId(entity.getUsuarioId())
                .plataforma(entity.getPlataforma())
                .ativo(entity.isAtivo())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
