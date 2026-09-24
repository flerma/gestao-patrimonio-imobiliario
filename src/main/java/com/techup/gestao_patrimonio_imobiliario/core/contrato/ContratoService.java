package com.techup.gestao_patrimonio_imobiliario.core.contrato;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.contrato.ContratoRequest;
import com.techup.gestao_patrimonio_imobiliario.core.auth.AutenticacaoAtual;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusImovel;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoGarantia;
import com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel.PagamentoAluguelService;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoRepository;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoMapper;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelEntity;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelRepository;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoRepository;

@Service
@Transactional
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ImovelRepository imovelRepository;
    private final InquilinoRepository inquilinoRepository;
    private final PagamentoAluguelService pagamentoAluguelService;

    public ContratoService(ContratoRepository contratoRepository,
                           ImovelRepository imovelRepository,
                           InquilinoRepository inquilinoRepository,
                           PagamentoAluguelService pagamentoAluguelService) {
        this.contratoRepository = contratoRepository;
        this.imovelRepository = imovelRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.pagamentoAluguelService = pagamentoAluguelService;
    }

    public Contrato criar(ContratoRequest request) {
        ImovelEntity imovelEntity = buscarImovelEntity(request.getImovelId());
        InquilinoEntity inquilinoEntity = buscarInquilinoEntity(request.getInquilinoId());
        LocalDateTime agora = LocalDateTime.now();
        StatusContrato statusContrato = request.getStatus() != null ? request.getStatus() : StatusContrato.RASCUNHO;
        ContratoEntity entity = ContratoEntity.builder()
                .id(UUID.randomUUID())
                .imovel(imovelEntity)
                .inquilino(inquilinoEntity)
                .tipo(request.getTipo())
                .status(statusContrato)
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .valorAluguel(request.getValorAluguel())
                .diaVencimento(request.getDiaVencimento())
                .indiceReajuste(request.getIndiceReajuste())
                .percentualReajuste(request.getPercentualReajuste())
                .periodoReajuste(request.getPeriodoReajuste())
                .tipoGarantia(request.getTipoGarantia() != null ? request.getTipoGarantia() : TipoGarantia.SEM_GARANTIA)
                .valorGarantia(request.getValorGarantia())
                .observacoes(request.getObservacoes())
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        ContratoEntity salvo = contratoRepository.save(entity);
        marcarImovelComoAlugadoSeAplicavel(imovelEntity, statusContrato);
        pagamentoAluguelService.gerarParaContrato(
                salvo, Boolean.TRUE.equals(request.getMarcarParcelasAnterioresComoPagas()));
        return ContratoMapper.toDomain(salvo);
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        return contratoRepository.findAllByImovelUsuarioId(AutenticacaoAtual.usuarioId()).stream()
                .map(ContratoMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Contrato buscarPorId(UUID id) {
        return ContratoMapper.toDomain(buscarContratoEntity(id));
    }

    public Contrato atualizar(UUID id, ContratoRequest request) {
        ContratoEntity existente = buscarContratoEntity(id);
        ImovelEntity imovelAnterior = existente.getImovel();
        ImovelEntity imovelEntity = buscarImovelEntity(request.getImovelId());
        InquilinoEntity inquilinoEntity = buscarInquilinoEntity(request.getInquilinoId());
        existente.setImovel(imovelEntity);
        existente.setInquilino(inquilinoEntity);
        existente.setTipo(request.getTipo());
        StatusContrato novoStatus = request.getStatus() != null ? request.getStatus() : existente.getStatus();
        existente.setStatus(novoStatus);
        existente.setDataInicio(request.getDataInicio());
        existente.setDataFim(request.getDataFim());
        existente.setValorAluguel(request.getValorAluguel());
        existente.setDiaVencimento(request.getDiaVencimento());
        existente.setIndiceReajuste(request.getIndiceReajuste());
        existente.setPercentualReajuste(request.getPercentualReajuste());
        existente.setPeriodoReajuste(request.getPeriodoReajuste());
        existente.setTipoGarantia(request.getTipoGarantia() != null ? request.getTipoGarantia() : existente.getTipoGarantia());
        existente.setValorGarantia(request.getValorGarantia());
        existente.setObservacoes(request.getObservacoes());
        existente.setDataAtualizacao(LocalDateTime.now());
        ContratoEntity salvo = contratoRepository.save(existente);

        marcarImovelComoAlugadoSeAplicavel(imovelEntity, novoStatus);
        if (novoStatus != StatusContrato.ATIVO) {
            liberarImovelSeSemContratoAtivo(imovelEntity, salvo.getId());
        }
        if (imovelAnterior != null && !imovelAnterior.getId().equals(imovelEntity.getId())) {
            liberarImovelSeSemContratoAtivo(imovelAnterior, salvo.getId());
        }
        pagamentoAluguelService.reconciliarParaContrato(
                salvo, Boolean.TRUE.equals(request.getMarcarParcelasAnterioresComoPagas()));
        if (Boolean.TRUE.equals(request.getAtualizarVencimentoParcelasFuturas())) {
            pagamentoAluguelService.atualizarVencimentoParcelasFuturas(salvo);
        }
        return ContratoMapper.toDomain(salvo);
    }

    public void deletar(UUID id) {
        ContratoEntity contrato = buscarContratoEntity(id);
        ImovelEntity imovel = contrato.getImovel();
        pagamentoAluguelService.removerPorContrato(id);
        contratoRepository.delete(contrato);
        if (imovel != null) {
            liberarImovelSeSemContratoAtivo(imovel, id);
        }
    }

    private boolean pertenceAoUsuarioAtual(UUID donoId) {
        return donoId != null && donoId.equals(AutenticacaoAtual.usuarioId());
    }

    /**
     * Quando um contrato passa a valer (status ATIVO), o imovel deixa de estar
     * disponivel e passa a constar como alugado. Nao mexe em imoveis de uso
     * proprio nem reverte um imovel ja alugado.
     */
    private void marcarImovelComoAlugadoSeAplicavel(ImovelEntity imovel, StatusContrato statusContrato) {
        if (statusContrato == StatusContrato.ATIVO && imovel.getStatus() == StatusImovel.DISPONIVEL) {
            imovel.setStatus(StatusImovel.ALUGADO);
            imovelRepository.save(imovel);
        }
    }

    /**
     * Quando um contrato deixa de valer (encerrado, rescindido ou excluido) e o
     * imovel nao possui nenhum outro contrato ATIVO, o imovel volta a ficar
     * disponivel. So altera imovel que esteja marcado como ALUGADO (nao mexe em
     * uso proprio). {@code contratoIdIgnorado} e desconsiderado na verificacao.
     */
    private void liberarImovelSeSemContratoAtivo(ImovelEntity imovel, UUID contratoIdIgnorado) {
        if (imovel.getStatus() != StatusImovel.ALUGADO) {
            return;
        }
        boolean temOutroContratoAtivo = contratoRepository
                .existsByImovelIdAndStatusAndIdNot(imovel.getId(), StatusContrato.ATIVO, contratoIdIgnorado);
        if (!temOutroContratoAtivo) {
            imovel.setStatus(StatusImovel.DISPONIVEL);
            imovelRepository.save(imovel);
        }
    }

    /** Busca o contrato garantindo que o imovel pertence ao usuario autenticado (404 caso contrario). */
    private ContratoEntity buscarContratoEntity(UUID id) {
        return contratoRepository.findByIdAndImovelUsuarioId(id, AutenticacaoAtual.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato nao encontrado: " + id));
    }

    /** Busca o imovel garantindo que pertence ao usuario autenticado (404 caso contrario). */
    private ImovelEntity buscarImovelEntity(UUID imovelId) {
        ImovelEntity imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado: " + imovelId));
        if (!pertenceAoUsuarioAtual(imovel.getUsuario() != null ? imovel.getUsuario().getId() : null)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado: " + imovelId);
        }
        return imovel;
    }

    /** Busca o inquilino garantindo que pertence ao usuario autenticado (404 caso contrario). */
    private InquilinoEntity buscarInquilinoEntity(UUID inquilinoId) {
        InquilinoEntity inquilino = inquilinoRepository.findById(inquilinoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquilino nao encontrado: " + inquilinoId));
        if (!pertenceAoUsuarioAtual(inquilino.getUsuario() != null ? inquilino.getUsuario().getId() : null)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquilino nao encontrado: " + inquilinoId);
        }
        return inquilino;
    }
}
