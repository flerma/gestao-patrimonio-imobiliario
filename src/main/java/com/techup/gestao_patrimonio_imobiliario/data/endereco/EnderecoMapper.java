package com.techup.gestao_patrimonio_imobiliario.data.endereco;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;

public final class EnderecoMapper {

    private EnderecoMapper() {
    }

    public static EnderecoEntity toEmbeddable(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return EnderecoEntity.builder()
                .cep(endereco.getCep())
                .logradouro(endereco.getLogradouro())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .bairro(endereco.getBairro())
                .cidade(endereco.getCidade())
                .estado(endereco.getEstado())
                .pais(endereco.getPais())
                .build();
    }

    public static Endereco toDomain(EnderecoEntity embeddable) {
        if (embeddable == null) {
            return null;
        }
        return Endereco.builder()
                .cep(embeddable.getCep())
                .logradouro(embeddable.getLogradouro())
                .numero(embeddable.getNumero())
                .complemento(embeddable.getComplemento())
                .bairro(embeddable.getBairro())
                .cidade(embeddable.getCidade())
                .estado(embeddable.getEstado())
                .pais(embeddable.getPais())
                .build();
    }
}
