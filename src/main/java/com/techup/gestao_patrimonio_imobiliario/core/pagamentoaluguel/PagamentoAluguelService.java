package com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel.PagamentoAluguelRequest;
import com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel.RegistrarPagamentoRequest;
import com.techup.gestao_patrimonio_imobiliario.core.auth.AutenticacaoAtual;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusPagamentoAluguel;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoRepository;
import com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel.PagamentoAluguelEntity;
import com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel.PagamentoAluguelRepository;
import com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel.PagamentoAluguelMapper;

@Service
@Transactional
public class PagamentoAluguelService {

    private final PagamentoAluguelRepository pagamentoAluguelRepository;
    private final ContratoRepository contratoRepository;

    public PagamentoAluguelService(PagamentoAluguelRepository pagamentoAluguelRepository,
                                   ContratoRepository contratoRepository) {
        this.pagamentoAluguelRepository = pagamentoAluguelRepository;
        this.contratoRepository = contratoRepository;
    }

    /**
     * Gera automaticamente a serie de alugueis do contrato: uma cobranca por mes,
     * do mes de inicio ao mes de fim do contrato, com vencimento no dia configurado
     * em {@code diaVencimento} (ajustado para o ultimo dia quando o mes for mais curto).
     * Idempotente: competencias ja existentes para o contrato sao ignoradas.
     */
    public List<PagamentoAluguel> gerarParaContrato(ContratoEntity contrato) {
        if (contrato.getDataInicio() == null || contrato.getDataFim() == null) {
            return List.of();
        }
        YearMonth primeira = YearMonth.from(contrato.getDataInicio());
        YearMonth ultima = YearMonth.from(contrato.getDataFim());
        if (ultima.isBefore(primeira)) {
            return List.of();
        }

        LocalDateTime agora = LocalDateTime.now();
        List<PagamentoAluguelEntity> novos = new ArrayList<>();
        for (YearMonth competencia = primeira; !competencia.isAfter(ultima); competencia = competencia.plusMonths(1)) {
            if (pagamentoAluguelRepository.existsByContratoIdAndCompetencia(contrato.getId(), competencia.atDay(1))) {
                continue;
            }
            int dia = Math.min(contrato.getDiaVencimento(), competencia.lengthOfMonth());
            novos.add(PagamentoAluguelEntity.builder()
                    .id(UUID.randomUUID())
                    .contrato(contrato)
                    .competencia(competencia.atDay(1))
                    .dataVencimento(competencia.atDay(dia))
                    .valorPrevisto(contrato.getValorAluguel())
                    .status(StatusPagamentoAluguel.PENDENTE)
                    .dataCriacao(agora)
                    .dataAtualizacao(agora)
                    .build());
        }
        return pagamentoAluguelRepository.saveAll(novos).stream()
                .map(PagamentoAluguelMapper::toDomain)
                .toList();
    }

    public void removerPorContrato(UUID contratoId) {
        pagamentoAluguelRepository.deleteByContratoId(contratoId);
    }

    /**
     * Reconcilia a serie de cobrancas apos uma alteracao na vigencia do
     * contrato (dataInicio/dataFim):
     * <ul>
     *   <li>gera (via {@link #gerarParaContrato}, idempotente) as competencias
     *       que passaram a fazer parte do periodo - cobre tanto adiantar o
     *       inicio quanto prorrogar o fim;</li>
     *   <li>remove as cobrancas cuja competencia ficou fora do novo periodo -
     *       tanto as anteriores ao novo inicio quanto as posteriores ao novo
     *       fim - mesmo que ja estejam pagas, espelhando o comportamento ja
     *       existente de excluir o contrato inteiro.</li>
     * </ul>
     */
    public void reconciliarParaContrato(ContratoEntity contrato) {
        if (contrato.getDataInicio() == null || contrato.getDataFim() == null) {
            return;
        }
        gerarParaContrato(contrato);
        pagamentoAluguelRepository.deleteByContratoIdAndCompetenciaBefore(
                contrato.getId(), YearMonth.from(contrato.getDataInicio()).atDay(1));
        pagamentoAluguelRepository.deleteByContratoIdAndCompetenciaAfter(
                contrato.getId(), YearMonth.from(contrato.getDataFim()).atDay(1));
    }

    public PagamentoAluguel criar(PagamentoAluguelRequest request) {
        ContratoEntity contrato = buscarContratoEntity(request.getContratoId());
        LocalDateTime agora = LocalDateTime.now();
        PagamentoAluguelEntity entity = PagamentoAluguelEntity.builder()
                .id(UUID.randomUUID())
                .contrato(contrato)
                .competencia(request.getCompetencia().atDay(1))
                .dataVencimento(request.getDataVencimento())
                .valorPrevisto(request.getValorPrevisto())
                .valorPago(request.getValorPago())
                .dataPagamento(request.getDataPagamento())
                .status(request.getStatus() != null ? request.getStatus() : StatusPagamentoAluguel.PENDENTE)
                .formaPagamento(request.getFormaPagamento())
                .observacoes(request.getObservacoes())
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        return PagamentoAluguelMapper.toDomain(pagamentoAluguelRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PagamentoAluguel> listar(UUID contratoId) {
        List<PagamentoAluguelEntity> entidades;
        if (contratoId != null) {
            buscarContratoEntity(contratoId); // valida que o contrato pertence ao usuario autenticado
            entidades = pagamentoAluguelRepository.findByContratoIdOrderByCompetenciaAsc(contratoId);
        } else {
            entidades = pagamentoAluguelRepository
                    .findAllByContratoImovelUsuarioIdOrderByDataVencimentoAsc(AutenticacaoAtual.usuarioId());
        }
        return entidades.stream()
                .map(PagamentoAluguelMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagamentoAluguel buscarPorId(UUID id) {
        return PagamentoAluguelMapper.toDomain(buscarEntity(id));
    }

    public PagamentoAluguel atualizar(UUID id, PagamentoAluguelRequest request) {
        PagamentoAluguelEntity existente = buscarEntity(id);
        ContratoEntity contrato = buscarContratoEntity(request.getContratoId());
        existente.setContrato(contrato);
        existente.setCompetencia(request.getCompetencia().atDay(1));
        existente.setDataVencimento(request.getDataVencimento());
        existente.setValorPrevisto(request.getValorPrevisto());
        existente.setValorPago(request.getValorPago());
        existente.setDataPagamento(request.getDataPagamento());
        existente.setStatus(request.getStatus() != null ? request.getStatus() : existente.getStatus());
        existente.setFormaPagamento(request.getFormaPagamento());
        existente.setObservacoes(request.getObservacoes());
        existente.setDataAtualizacao(LocalDateTime.now());
        return PagamentoAluguelMapper.toDomain(pagamentoAluguelRepository.save(existente));
    }

    /**
     * Marca a cobranca como quitada integralmente: valorPago = valorPrevisto,
     * status = PAGO, com a data e forma de pagamento informadas.
     */
    public PagamentoAluguel registrarPagamento(UUID id, RegistrarPagamentoRequest request) {
        PagamentoAluguelEntity existente = buscarEntity(id);

        existente.setValorPago(existente.getValorPrevisto());
        existente.setDataPagamento(request.getDataPagamento());
        existente.setFormaPagamento(request.getFormaPagamento());
        existente.setStatus(StatusPagamentoAluguel.PAGO);
        if (request.getObservacoes() != null) {
            existente.setObservacoes(request.getObservacoes());
        }
        existente.setDataAtualizacao(LocalDateTime.now());
        return PagamentoAluguelMapper.toDomain(pagamentoAluguelRepository.save(existente));
    }

    public void deletar(UUID id) {
        pagamentoAluguelRepository.delete(buscarEntity(id));
    }

    /** Busca o pagamento garantindo que o contrato pertence ao usuario autenticado (404 caso contrario). */
    private PagamentoAluguelEntity buscarEntity(UUID id) {
        PagamentoAluguelEntity entity = pagamentoAluguelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pagamento de aluguel nao encontrado: " + id));
        UUID donoId = entity.getContrato() != null && entity.getContrato().getImovel() != null
                ? entity.getContrato().getImovel().getUsuario().getId()
                : null;
        if (!AutenticacaoAtual.usuarioId().equals(donoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento de aluguel nao encontrado: " + id);
        }
        return entity;
    }

    /** Busca o contrato garantindo que o imovel pertence ao usuario autenticado (404 caso contrario). */
    private ContratoEntity buscarContratoEntity(UUID contratoId) {
        return contratoRepository.findByIdAndImovelUsuarioId(contratoId, AutenticacaoAtual.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Contrato nao encontrado: " + contratoId));
    }
}
