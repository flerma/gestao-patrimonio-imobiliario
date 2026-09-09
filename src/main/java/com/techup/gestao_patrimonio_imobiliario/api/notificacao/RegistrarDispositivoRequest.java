package com.techup.gestao_patrimonio_imobiliario.api.notificacao;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistrarDispositivoRequest {

    @NotBlank
    String expoPushToken;

    /** Proprietario selecionado no app (opcional). */
    UUID usuarioId;

    /** "ios" | "android" (opcional). */
    String plataforma;
}
