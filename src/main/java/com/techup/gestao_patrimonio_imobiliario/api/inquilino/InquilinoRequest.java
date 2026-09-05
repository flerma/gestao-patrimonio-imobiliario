package com.techup.gestao_patrimonio_imobiliario.api.inquilino;

import java.time.LocalDate;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusInquilino;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoPessoa;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InquilinoRequest {

    @NotNull
    TipoPessoa tipoPessoa;

    @NotBlank
    String nome;

    @NotBlank
    String documento;

    @Email
    String email;

    String telefone;

    LocalDate dataNascimento;

    Endereco endereco;

    String observacoes;

    StatusInquilino status;
}
