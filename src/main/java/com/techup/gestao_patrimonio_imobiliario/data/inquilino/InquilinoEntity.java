package com.techup.gestao_patrimonio_imobiliario.data.inquilino;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusInquilino;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoPessoa;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inquilinos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquilinoEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "documento", nullable = false)
    private String documento;

    @Column(name = "email")
    private String email;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Embedded
    private EnderecoEntity endereco;

    @Column(name = "observacoes")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusInquilino status;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;
}
