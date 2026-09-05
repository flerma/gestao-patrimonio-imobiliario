package com.techup.gestao_patrimonio_imobiliario.data.inquilino;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquilinoJpaRepository extends JpaRepository<InquilinoEntity, UUID> {
}
