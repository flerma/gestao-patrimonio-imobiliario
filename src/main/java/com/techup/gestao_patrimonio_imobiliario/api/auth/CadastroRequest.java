package com.techup.gestao_patrimonio_imobiliario.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CadastroRequest {

    @NotBlank
    String nome;

    @NotBlank
    @Email
    String email;

    @NotBlank
    String telefone;

    @NotBlank
    @Size(min = 6)
    String senha;

    @NotBlank
    String confirmarSenha;
}
