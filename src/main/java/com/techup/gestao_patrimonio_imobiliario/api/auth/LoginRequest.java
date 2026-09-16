package com.techup.gestao_patrimonio_imobiliario.api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginRequest {

    @NotBlank
    String usuario;

    @NotBlank
    String senha;
}
