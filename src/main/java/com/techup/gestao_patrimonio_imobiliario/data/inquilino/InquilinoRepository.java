package com.techup.gestao_patrimonio_imobiliario.data.inquilino;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquilinoRepository extends JpaRepository<InquilinoEntity, UUID> {

    List<InquilinoEntity> findAllByUsuarioId(UUID usuarioId);

    Optional<InquilinoEntity> findByIdAndUsuarioId(UUID id, UUID usuarioId);

    boolean existsByUsuarioIdAndDocumento(UUID usuarioId, String documento);

    boolean existsByUsuarioIdAndDocumentoAndIdNot(UUID usuarioId, String documento, UUID id);
}
