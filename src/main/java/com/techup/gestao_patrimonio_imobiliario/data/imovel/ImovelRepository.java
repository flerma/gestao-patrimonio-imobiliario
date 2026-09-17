package com.techup.gestao_patrimonio_imobiliario.data.imovel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImovelRepository extends JpaRepository<ImovelEntity, UUID> {

    List<ImovelEntity> findAllByUsuarioId(UUID usuarioId);

    Optional<ImovelEntity> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
