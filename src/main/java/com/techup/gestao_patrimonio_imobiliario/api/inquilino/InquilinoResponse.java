package com.techup.gestao_patrimonio_imobiliario.api.inquilino;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusInquilino;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoPessoa;
import com.techup.gestao_patrimonio_imobiliario.core.inquilino.Inquilino;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class InquilinoResponse {

    UUID id;
    TipoPessoa tipoPessoa;
    String nome;
    String documento;
    String email;
    String telefone;
    LocalDate dataNascimento;
    Endereco endereco;
    String observacoes;
    StatusInquilino status;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;

    public static InquilinoResponse from(Inquilino inquilino) {
        if (inquilino == null) {
            return null;
        }
        return InquilinoResponse.builder()
                .id(inquilino.getId())
                .tipoPessoa(inquilino.getTipoPessoa())
                .nome(inquilino.getNome())
                .documento(inquilino.getDocumento())
                .email(inquilino.getEmail())
                .telefone(inquilino.getTelefone())
                .dataNascimento(inquilino.getDataNascimento())
                .endereco(inquilino.getEndereco())
                .observacoes(inquilino.getObservacoes())
                .status(inquilino.getStatus())
                .dataCriacao(inquilino.getDataCriacao())
                .dataAtualizacao(inquilino.getDataAtualizacao())
                .build();
    }
}
