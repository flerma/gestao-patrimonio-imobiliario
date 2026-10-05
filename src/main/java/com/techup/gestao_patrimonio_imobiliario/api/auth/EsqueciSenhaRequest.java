package com.techup.gestao_patrimonio_imobiliario.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EsqueciSenhaRequest {

    @NotBlank
    @Email
    String email;
}
