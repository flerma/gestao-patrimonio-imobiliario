package com.techup.gestao_patrimonio_imobiliario.api.usuario;

import com.techup.gestao_patrimonio_imobiliario.core.enums.ProvedorAutenticacao;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioRequest {

    @NotBlank
    String nome;

    @NotBlank
    @Email
    String email;

    @NotNull
    ProvedorAutenticacao provedorAutenticacao;

    String idUsuarioProvedor;

    StatusUsuario status;
}
