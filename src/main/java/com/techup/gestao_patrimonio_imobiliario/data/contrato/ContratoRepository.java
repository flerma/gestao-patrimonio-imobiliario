package com.techup.gestao_patrimonio_imobiliario.data.contrato;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<ContratoEntity, UUID> {
}
