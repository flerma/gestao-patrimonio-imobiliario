package com.techup.gestao_patrimonio_imobiliario.data.inquilino;

import com.techup.gestao_patrimonio_imobiliario.core.inquilino.Inquilino;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoMapper;

public final class InquilinoMapper {

    private InquilinoMapper() {
    }

    public static InquilinoEntity toEntity(Inquilino inquilino) {
        if (inquilino == null) {
            return null;
        }
        return InquilinoEntity.builder()
                .id(inquilino.getId())
                .tipoPessoa(inquilino.getTipoPessoa())
                .nome(inquilino.getNome())
                .documento(inquilino.getDocumento())
                .email(inquilino.getEmail())
                .telefone(inquilino.getTelefone())
                .dataNascimento(inquilino.getDataNascimento())
                .endereco(EnderecoMapper.toEmbeddable(inquilino.getEndereco()))
                .observacoes(inquilino.getObservacoes())
                .status(inquilino.getStatus())
                .dataCriacao(inquilino.getDataCriacao())
                .dataAtualizacao(inquilino.getDataAtualizacao())
                .build();
    }

    public static Inquilino toDomain(InquilinoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Inquilino.builder()
                .id(entity.getId())
                .tipoPessoa(entity.getTipoPessoa())
                .nome(entity.getNome())
                .documento(entity.getDocumento())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .dataNascimento(entity.getDataNascimento())
                .endereco(EnderecoMapper.toDomain(entity.getEndereco()))
                .observacoes(entity.getObservacoes())
                .status(entity.getStatus())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
