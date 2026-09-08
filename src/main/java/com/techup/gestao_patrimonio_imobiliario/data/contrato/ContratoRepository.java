package com.techup.gestao_patrimonio_imobiliario.data.contrato;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;

public interface ContratoRepository extends JpaRepository<ContratoEntity, UUID> {

    boolean existsByImovelIdAndStatusAndIdNot(UUID imovelId, StatusContrato status, UUID id);
}
