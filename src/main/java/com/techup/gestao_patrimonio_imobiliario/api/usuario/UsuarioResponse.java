package com.techup.gestao_patrimonio_imobiliario.api.usuario;

import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.ProvedorAutenticacao;
import com.techup.gestao_patrimonio_imobiliario.core.enums.RoleUsuario;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusUsuario;
import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UsuarioResponse {

    UUID id;
    String nome;
    String email;
    String telefone;
    ProvedorAutenticacao provedorAutenticacao;
    String idUsuarioProvedor;
    StatusUsuario status;
    RoleUsuario role;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;

    public static UsuarioResponse from(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .provedorAutenticacao(usuario.getProvedorAutenticacao())
                .idUsuarioProvedor(usuario.getIdUsuarioProvedor())
                .status(usuario.getStatus())
                .role(usuario.getRole())
                .dataCriacao(usuario.getDataCriacao())
                .dataAtualizacao(usuario.getDataAtualizacao())
                .build();
    }
}
