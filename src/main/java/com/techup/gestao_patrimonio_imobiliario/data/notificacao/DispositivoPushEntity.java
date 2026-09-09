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

/** Token de push (Expo) de um aparelho, opcionalmente vinculado a um proprietario. */
@Entity
@Table(name = "dispositivos_push")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoPushEntity {

    @Id
    private UUID id;

    @Column(name = "expo_push_token", nullable = false, unique = true)
    private String expoPushToken;

    /** Proprietario selecionado no app no momento do registro (pode ser nulo). */
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "plataforma")
    private String plataforma;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;
}
