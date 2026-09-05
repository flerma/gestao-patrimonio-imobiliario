package com.techup.gestao_patrimonio_imobiliario.data.usuario;

import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return UsuarioEntity.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .provedorAutenticacao(usuario.getProvedorAutenticacao())
                .idUsuarioProvedor(usuario.getIdUsuarioProvedor())
                .status(usuario.getStatus())
                .dataCriacao(usuario.getDataCriacao())
                .dataAtualizacao(usuario.getDataAtualizacao())
                .build();
    }

    public static Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        return Usuario.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .provedorAutenticacao(entity.getProvedorAutenticacao())
                .idUsuarioProvedor(entity.getIdUsuarioProvedor())
                .status(entity.getStatus())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
