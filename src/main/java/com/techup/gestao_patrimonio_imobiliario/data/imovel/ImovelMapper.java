package com.techup.gestao_patrimonio_imobiliario.data.imovel;

import com.techup.gestao_patrimonio_imobiliario.core.imovel.Imovel;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoMapper;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioMapper;

public final class ImovelMapper {

    private ImovelMapper() {
    }

    public static ImovelEntity toEntity(Imovel imovel, UsuarioEntity usuarioEntity) {
        if (imovel == null) {
            return null;
        }
        return ImovelEntity.builder()
                .id(imovel.getId())
                .usuario(usuarioEntity)
                .nome(imovel.getNome())
                .tipo(imovel.getTipo())
                .status(imovel.getStatus())
                .valorAquisicao(imovel.getValorAquisicao())
                .valorAtual(imovel.getValorAtual())
                .endereco(EnderecoMapper.toEmbeddable(imovel.getEndereco()))
                .dataCriacao(imovel.getDataCriacao())
                .dataAtualizacao(imovel.getDataAtualizacao())
                .build();
    }

    public static Imovel toDomain(ImovelEntity entity) {
        if (entity == null) {
            return null;
        }
        return Imovel.builder()
                .id(entity.getId())
                .usuario(UsuarioMapper.toDomain(entity.getUsuario()))
                .nome(entity.getNome())
                .tipo(entity.getTipo())
                .status(entity.getStatus())
                .valorAquisicao(entity.getValorAquisicao())
                .valorAtual(entity.getValorAtual())
                .endereco(EnderecoMapper.toDomain(entity.getEndereco()))
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
