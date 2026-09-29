package com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoAluguelRepository extends JpaRepository<PagamentoAluguelEntity, UUID> {

    List<PagamentoAluguelEntity> findByContratoIdOrderByCompetenciaAsc(UUID contratoId);

    /** Soma o valor efetivamente pago de todas as parcelas de todos os contratos do imovel (qualquer status). */
    @Query("select coalesce(sum(p.valorPago), 0) from PagamentoAluguelEntity p where p.contrato.imovel.id = :imovelId")
    BigDecimal sumValorPagoByImovelId(@Param("imovelId") UUID imovelId);

    List<PagamentoAluguelEntity> findAllByOrderByDataVencimentoAsc();

    List<PagamentoAluguelEntity> findAllByContratoImovelUsuarioIdOrderByDataVencimentoAsc(UUID usuarioId);

    boolean existsByContratoIdAndCompetencia(UUID contratoId, java.time.LocalDate competencia);

    void deleteByContratoId(UUID contratoId);

    /** Remove cobrancas de competencia anterior ao mes informado (novo inicio de vigencia). */
    void deleteByContratoIdAndCompetenciaBefore(UUID contratoId, java.time.LocalDate competencia);

    /** Remove cobrancas de competencia posterior ao mes informado (novo fim de vigencia). */
    void deleteByContratoIdAndCompetenciaAfter(UUID contratoId, java.time.LocalDate competencia);

    /** Cobrancas de competencia posterior ao mes informado (para reajustar o vencimento). */
    List<PagamentoAluguelEntity> findByContratoIdAndCompetenciaAfter(UUID contratoId, java.time.LocalDate competencia);

    /** Cobrancas de competencia igual ou posterior ao mes informado (para reajustar o valor previsto). */
    List<PagamentoAluguelEntity> findByContratoIdAndCompetenciaGreaterThanEqual(UUID contratoId, java.time.LocalDate competencia);
}
