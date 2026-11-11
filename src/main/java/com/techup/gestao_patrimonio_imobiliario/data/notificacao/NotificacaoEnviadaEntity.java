package com.techup.gestao_patrimonio_imobiliario.data.notificacao;

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

/** Push pontual ja enviado (ex.: aviso de reajuste), para nao repetir o envio. */
@Entity
@Table(name = "notificacoes_enviadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoEnviadaEntity {

    @Id
    private UUID id;

    @Column(name = "chave", nullable = false, unique = true, length = 150)
    private String chave;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;
}
