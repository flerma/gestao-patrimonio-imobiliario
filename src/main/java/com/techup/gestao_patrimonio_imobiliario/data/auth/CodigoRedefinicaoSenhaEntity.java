package com.techup.gestao_patrimonio_imobiliario.data.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "codigos_redefinicao_senha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoRedefinicaoSenhaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "codigo_hash", nullable = false)
    private String codigoHash;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
}
