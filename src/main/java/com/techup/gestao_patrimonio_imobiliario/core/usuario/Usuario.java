package com.techup.gestao_patrimonio_imobiliario.core.usuario;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.ProvedorAutenticacao;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusUsuario;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@With
@Builder
@Jacksonized
public class Usuario {

    UUID id;
    String nome;
    String email;
    ProvedorAutenticacao provedorAutenticacao;
    String idUsuarioProvedor;
    StatusUsuario status;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;
}
