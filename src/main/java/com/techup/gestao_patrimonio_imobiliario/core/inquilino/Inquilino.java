package com.techup.gestao_patrimonio_imobiliario.core.inquilino;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusInquilino;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoPessoa;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@With
@Builder
@Jacksonized
public class Inquilino {

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
}
