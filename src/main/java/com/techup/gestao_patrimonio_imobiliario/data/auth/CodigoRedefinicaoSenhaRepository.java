package com.techup.gestao_patrimonio_imobiliario.data.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoRedefinicaoSenhaRepository extends JpaRepository<CodigoRedefinicaoSenhaEntity, UUID> {

    /** Codigo mais recente do usuario que ainda nao foi usado nem invalidado. */
    Optional<CodigoRedefinicaoSenhaEntity> findFirstByUsuarioIdAndUsadoFalseOrderByDataCriacaoDesc(UUID usuarioId);

    List<CodigoRedefinicaoSenhaEntity> findByUsuarioIdAndUsadoFalse(UUID usuarioId);

    /** Codigo mais recente do usuario (usado ou nao) - para o intervalo minimo entre envios. */
    Optional<CodigoRedefinicaoSenhaEntity> findFirstByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    void deleteByUsuarioId(UUID usuarioId);
}
