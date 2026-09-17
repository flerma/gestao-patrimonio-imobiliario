package com.techup.gestao_patrimonio_imobiliario.data.inquilino;

import com.techup.gestao_patrimonio_imobiliario.core.inquilino.Inquilino;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoMapper;

public final class InquilinoMapper {

    private InquilinoMapper() {
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
