package com.techup.gestao_patrimonio_imobiliario.core.auth;

import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;

import lombok.Builder;
import lombok.Value;

/**
 * Par de tokens emitido no login (e o novo access token emitido no refresh).
 */
@Value
@Builder
public class TokenPair {

    String accessToken;
    String refreshToken;
    long expiresIn;
    Usuario usuario;
}
