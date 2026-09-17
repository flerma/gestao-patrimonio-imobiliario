package com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoAluguelRepository extends JpaRepository<PagamentoAluguelEntity, UUID> {

    List<PagamentoAluguelEntity> findByContratoIdOrderByCompetenciaAsc(UUID contratoId);

    List<PagamentoAluguelEntity> findAllByOrderByDataVencimentoAsc();

    List<PagamentoAluguelEntity> findAllByContratoImovelUsuarioIdOrderByDataVencimentoAsc(UUID usuarioId);

    boolean existsByContratoIdAndCompetencia(UUID contratoId, java.time.LocalDate competencia);

    void deleteByContratoId(UUID contratoId);
}
