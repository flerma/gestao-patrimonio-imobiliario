package com.techup.gestao_patrimonio_imobiliario.data.imovel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImovelJpaRepository extends JpaRepository<ImovelEntity, UUID> {
}
