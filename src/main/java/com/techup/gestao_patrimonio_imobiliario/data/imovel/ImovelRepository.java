package com.techup.gestao_patrimonio_imobiliario.data.imovel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImovelRepository extends JpaRepository<ImovelEntity, UUID> {
}
