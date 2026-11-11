package com.techup.gestao_patrimonio_imobiliario.data.notificacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoEnviadaRepository extends JpaRepository<NotificacaoEnviadaEntity, UUID> {

    boolean existsByChave(String chave);
}
