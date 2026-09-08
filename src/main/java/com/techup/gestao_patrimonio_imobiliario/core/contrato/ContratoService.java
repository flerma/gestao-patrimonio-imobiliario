package com.techup.gestao_patrimonio_imobiliario.core.contrato;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.contrato.ContratoRequest;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;
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
        ContratoEntity entity = ContratoEntity.builder()
                .id(UUID.randomUUID())
                .imovel(imovelEntity)
                .inquilino(inquilinoEntity)
                .tipo(request.getTipo())
                .status(request.getStatus() != null ? request.getStatus() : StatusContrato.RASCUNHO)
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
        pagamentoAluguelService.gerarParaContrato(salvo);
        return ContratoMapper.toDomain(salvo);
    }

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        return contratoRepository.findAll().stream()
                .map(ContratoMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Contrato buscarPorId(UUID id) {
        return ContratoMapper.toDomain(buscarContratoEntity(id));
    }

    public Contrato atualizar(UUID id, ContratoRequest request) {
        ContratoEntity existente = buscarContratoEntity(id);
        ImovelEntity imovelEntity = buscarImovelEntity(request.getImovelId());
        InquilinoEntity inquilinoEntity = buscarInquilinoEntity(request.getInquilinoId());
        existente.setImovel(imovelEntity);
        existente.setInquilino(inquilinoEntity);
        existente.setTipo(request.getTipo());
        existente.setStatus(request.getStatus() != null ? request.getStatus() : existente.getStatus());
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
        return ContratoMapper.toDomain(contratoRepository.save(existente));
    }

    public void deletar(UUID id) {
        if (!contratoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato nao encontrado: " + id);
        }
        pagamentoAluguelService.removerPorContrato(id);
        contratoRepository.deleteById(id);
    }

    private ContratoEntity buscarContratoEntity(UUID id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato nao encontrado: " + id));
    }

    private ImovelEntity buscarImovelEntity(UUID imovelId) {
        return imovelRepository.findById(imovelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado: " + imovelId));
    }

    private InquilinoEntity buscarInquilinoEntity(UUID inquilinoId) {
        return inquilinoRepository.findById(inquilinoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquilino nao encontrado: " + inquilinoId));
    }
}
