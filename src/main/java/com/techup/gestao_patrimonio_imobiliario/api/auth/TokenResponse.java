package com.techup.gestao_patrimonio_imobiliario.api.auth;

import com.techup.gestao_patrimonio_imobiliario.api.usuario.UsuarioResponse;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TokenResponse {

    String accessToken;
    String refreshToken;
    long expiresIn;
    UsuarioResponse usuario;
}
