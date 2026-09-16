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
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "revogado", nullable = false)
    private boolean revogado;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
}
