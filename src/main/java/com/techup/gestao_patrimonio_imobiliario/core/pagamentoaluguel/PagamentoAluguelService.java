package com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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
import com.techup.gestao_patrimonio_imobiliario.core.contrato.PrimeiraParcelaCalculator;
import com.techup.gestao_patrimonio_imobiliario.core.enums.FormaPagamento;
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
     * da primeira a ultima competencia do contrato, com vencimento no dia
     * configurado em {@code diaVencimento} (ajustado para o ultimo dia quando o
     * mes for mais curto). Idempotente: competencias ja existentes para o
     * contrato sao ignoradas. Contrato sem data de fim (vigencia por prazo
     * indeterminado) gera 12 parcelas a partir da primeira.
     *
     * <p>Competencias anteriores ao mes atual nascem, por padrao, PENDENTE
     * (e portanto em atraso, ja que o vencimento ja passou). Quando
     * {@code marcarAnterioresPagas} e true, essas competencias passadas
     * nascem ja quitadas: PAGO, valorPago = valorAluguel, dataPagamento =
     * a propria dataVencimento da parcela, formaPagamento = PIX. So afeta
     * competencias sendo criadas agora - nao altera parcelas ja existentes.
     *
     * <p>A data de vencimento da primeira parcela e exatamente
     * {@code contrato.getDataPrimeiraParcela()} (calculada a partir de
     * dataInicio + diaVencimento quando o contrato nao a informa
     * explicitamente - ver {@link PrimeiraParcelaCalculator}), mesmo que o
     * dia dela nao coincida com {@code diaVencimento}; as parcelas seguintes
     * vencem no dia configurado em {@code diaVencimento} dos meses
     * seguintes. Ver {@link #vencimentoInicial} e {@link #competenciaInicialEfetiva}.
     */
    public List<PagamentoAluguel> gerarParaContrato(ContratoEntity contrato, boolean marcarAnterioresPagas) {
        if (contrato.getDataInicio() == null) {
            return List.of();
        }
        YearMonth vencMes = vencimentoInicial(contrato);
        YearMonth compMes = competenciaInicialEfetiva(contrato);
        YearMonth ultimaVencMes = vencimentoFinal(contrato);
        if (ultimaVencMes.isBefore(vencMes)) {
            return List.of();
        }
        YearMonth primeiraVencMes = vencMes;

        LocalDateTime agora = LocalDateTime.now();
        YearMonth mesAtual = YearMonth.now();
        List<PagamentoAluguelEntity> novos = new ArrayList<>();
        for (; !vencMes.isAfter(ultimaVencMes); vencMes = vencMes.plusMonths(1), compMes = compMes.plusMonths(1)) {
            if (pagamentoAluguelRepository.existsByContratoIdAndCompetencia(contrato.getId(), compMes.atDay(1))) {
                continue;
            }
            boolean ehPrimeiraParcela = vencMes.equals(primeiraVencMes);
            LocalDate dataVencimento;
            if (ehPrimeiraParcela && contrato.getDataPrimeiraParcela() != null) {
                // A primeira parcela respeita exatamente a data informada/confirmada
                // pelo usuario, mesmo que o dia dela nao coincida com diaVencimento.
                dataVencimento = contrato.getDataPrimeiraParcela();
            } else {
                int dia = Math.min(contrato.getDiaVencimento(), vencMes.lengthOfMonth());
                dataVencimento = vencMes.atDay(dia);
            }
            BigDecimal valorPrevisto = (ehPrimeiraParcela && contrato.getValorPrimeiraParcela() != null)
                    ? contrato.getValorPrimeiraParcela()
                    : contrato.getValorAluguel();
            boolean quitarComoPaga = marcarAnterioresPagas && compMes.isBefore(mesAtual);
            novos.add(PagamentoAluguelEntity.builder()
                    .id(UUID.randomUUID())
                    .contrato(contrato)
                    .competencia(compMes.atDay(1))
                    .dataVencimento(dataVencimento)
                    .valorPrevisto(valorPrevisto)
                    .valorPago(quitarComoPaga ? valorPrevisto : null)
                    .dataPagamento(quitarComoPaga ? dataVencimento : null)
                    .formaPagamento(quitarComoPaga ? FormaPagamento.PIX : null)
                    .status(quitarComoPaga ? StatusPagamentoAluguel.PAGO : StatusPagamentoAluguel.PENDENTE)
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
     * Ajusta a dataVencimento das parcelas com competencia posterior ao mes
     * atual para o dia de vencimento atual do contrato - usado quando o
     * usuario altera o dia de vencimento de um contrato existente e confirma
     * que quer propagar a mudanca para as parcelas futuras (as do mes atual
     * e anteriores nunca sao tocadas).
     */
    public void atualizarVencimentoParcelasFuturas(ContratoEntity contrato) {
        YearMonth mesAtual = YearMonth.now();
        List<PagamentoAluguelEntity> futuras = pagamentoAluguelRepository
                .findByContratoIdAndCompetenciaAfter(contrato.getId(), mesAtual.atDay(1));
        if (futuras.isEmpty()) {
            return;
        }
        long deslocamento = ChronoUnit.MONTHS.between(
                competenciaInicialEfetiva(contrato).atDay(1), vencimentoInicial(contrato).atDay(1));
        LocalDateTime agora = LocalDateTime.now();
        for (PagamentoAluguelEntity pagamento : futuras) {
            YearMonth competencia = YearMonth.from(pagamento.getCompetencia());
            YearMonth vencMes = competencia.plusMonths(deslocamento);
            int dia = Math.min(contrato.getDiaVencimento(), vencMes.lengthOfMonth());
            pagamento.setDataVencimento(vencMes.atDay(dia));
            pagamento.setDataAtualizacao(agora);
        }
        pagamentoAluguelRepository.saveAll(futuras);
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
    public void reconciliarParaContrato(ContratoEntity contrato, boolean marcarAnterioresPagas) {
        if (contrato.getDataInicio() == null) {
            return;
        }
        gerarParaContrato(contrato, marcarAnterioresPagas);
        pagamentoAluguelRepository.deleteByContratoIdAndCompetenciaBefore(
                contrato.getId(), competenciaInicialEfetiva(contrato).atDay(1));
        pagamentoAluguelRepository.deleteByContratoIdAndCompetenciaAfter(
                contrato.getId(), competenciaFinal(contrato).atDay(1));
    }

    /**
     * Mes de vencimento da primeira parcela: o de {@code dataPrimeiraParcela},
     * quando o contrato a informa. Contratos criados antes da introducao
     * desse campo (legado, {@code dataPrimeiraParcela == null}) caem no
     * comportamento anterior, calculado apenas a partir de
     * dataInicio/diaVencimento - ver {@link #competenciaInicialLegado}.
     */
    private YearMonth vencimentoInicial(ContratoEntity contrato) {
        return contrato.getDataPrimeiraParcela() != null
                ? YearMonth.from(contrato.getDataPrimeiraParcela())
                : competenciaInicialLegado(contrato);
    }

    /**
     * Competencia (mes de referencia) da primeira parcela - ver
     * {@link PrimeiraParcelaCalculator#competencia}. Para contratos legados
     * sem dataPrimeiraParcela, competencia e vencimento coincidem, exatamente
     * como no comportamento anterior a esse campo.
     */
    private YearMonth competenciaInicialEfetiva(ContratoEntity contrato) {
        return contrato.getDataPrimeiraParcela() != null
                ? PrimeiraParcelaCalculator.competencia(contrato.getDataInicio(), contrato.getDataPrimeiraParcela())
                : competenciaInicialLegado(contrato);
    }

    /**
     * Comportamento legado (pre dataPrimeiraParcela): o mes de inicio, a
     * menos que uma das situacoes abaixo empurre para o mes seguinte:
     * <ul>
     *   <li>o vencimento desse mes (dia configurado, clampado ao tamanho do
     *       mes) cai antes da propria data de inicio - ex.: contrato com
     *       inicio dia 15 e vencimento todo dia 10: o vencimento do mes de
     *       inicio seria dia 10, antes do inquilino sequer ter comecado o
     *       contrato;</li>
     *   <li>a data de inicio esta no mes corrente (o mes de "hoje") mas
     *       ainda nao chegou - ex.: hoje e dia 5 e o contrato comeca dia 10
     *       deste mesmo mes: como a vigencia ainda nao comecou de fato, a
     *       primeira parcela so nasce no mes seguinte, independente do dia
     *       de vencimento configurado.</li>
     * </ul>
     */
    private YearMonth competenciaInicialLegado(ContratoEntity contrato) {
        LocalDate dataInicio = contrato.getDataInicio();
        YearMonth mesInicio = YearMonth.from(dataInicio);
        int dia = Math.min(contrato.getDiaVencimento(), mesInicio.lengthOfMonth());
        LocalDate vencimentoMesInicio = mesInicio.atDay(dia);
        LocalDate hoje = LocalDate.now();

        boolean vencimentoAntesDoInicio = vencimentoMesInicio.isBefore(dataInicio);
        boolean inicioFuturoNoMesCorrente =
                mesInicio.equals(YearMonth.from(hoje)) && dataInicio.isAfter(hoje);

        return (vencimentoAntesDoInicio || inicioFuturoNoMesCorrente)
                ? mesInicio.plusMonths(1)
                : mesInicio;
    }

    /**
     * Ultimo mes de vencimento do contrato: o de dataFim, quando informada,
     * ou o 12o mes a partir da primeira parcela, para contratos por prazo
     * indeterminado.
     */
    private YearMonth vencimentoFinal(ContratoEntity contrato) {
        return contrato.getDataFim() != null
                ? YearMonth.from(contrato.getDataFim())
                : vencimentoInicial(contrato).plusMonths(11);
    }

    /**
     * Ultima competencia do contrato, na mesma escala de {@link #vencimentoFinal}
     * mas expressa em competencia (pode diferir em ate um mes do vencimento
     * - ver {@link #competenciaInicialEfetiva}).
     */
    private YearMonth competenciaFinal(ContratoEntity contrato) {
        long deslocamento = ChronoUnit.MONTHS.between(
                vencimentoInicial(contrato).atDay(1), vencimentoFinal(contrato).atDay(1));
        return competenciaInicialEfetiva(contrato).plusMonths(deslocamento);
    }

    public PagamentoAluguel criar(PagamentoAluguelRequest request) {
        ContratoEntity contrato = buscarContratoEntity(request.getContratoId());
        if (pagamentoAluguelRepository.existsByContratoIdAndCompetencia(
                contrato.getId(), request.getCompetencia().atDay(1))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ja existe um aluguel cadastrado para essa competencia neste contrato.");
        }
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
