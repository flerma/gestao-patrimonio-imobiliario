package com.techup.gestao_patrimonio_imobiliario.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
            message = "A senha deve ter ao menos 8 caracteres, uma letra maiuscula, "
                    + "uma letra minuscula, um numero e um caractere especial")
    String senha;

    @NotBlank
    String confirmarSenha;
}
