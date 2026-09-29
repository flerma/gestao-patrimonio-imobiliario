package com.techup.gestao_patrimonio_imobiliario.api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class GoogleLoginRequest {

    /** ID token (JWT) emitido pelo Google Identity Services / Google Sign-In no cliente. */
    @NotBlank
    String idToken;
}
