package com.techup.gestao_patrimonio_imobiliario.core.alerta;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techup.gestao_patrimonio_imobiliario.core.enums.IndiceReajuste;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusContrato;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusImovel;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoRepository;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelEntity;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelRepository;

/**
 * Reavalia, a partir do banco, as mesmas regras de alerta do dashboard.
 * Usado tanto pelo endpoint {@code GET /api/alertas} quanto pelo envio de
 * notificacoes push.
 */
@Service
@Transactional(readOnly = true)
public class AlertaService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ImovelRepository imovelRepository;
    private final ContratoRepository contratoRepository;

    public AlertaService(ImovelRepository imovelRepository, ContratoRepository contratoRepository) {
        this.imovelRepository = imovelRepository;
        this.contratoRepository = contratoRepository;
    }

    /**
     * @param usuarioId proprietario a considerar; {@code null} => consolidado (todos).
     */
    public List<Alerta> listarParaUsuario(UUID usuarioId) {
        return listar(usuarioId, LocalDate.now());
    }

    List<Alerta> listar(UUID usuarioId, LocalDate hoje) {
        List<ImovelEntity> imoveis = imovelRepository.findAll();
        List<ContratoEntity> contratos = contratoRepository.findAll();

        List<ImovelEntity> imoveisUsuario = usuarioId == null
                ? imoveis
                : imoveis.stream()
                        .filter(i -> i.getUsuario() != null && usuarioId.equals(i.getUsuario().getId()))
                        .toList();
        Set<UUID> idsImoveisUsuario = imoveisUsuario.stream()
                .map(ImovelEntity::getId)
                .collect(Collectors.toSet());
        List<ContratoEntity> contratosUsuario = usuarioId == null
                ? contratos
                : contratos.stream()
                        .filter(c -> c.getImovel() != null && idsImoveisUsuario.contains(c.getImovel().getId()))
                        .toList();

        LocalDate em60Dias = hoje.plusDays(60);
        LocalDate em30Dias = hoje.plusDays(30);

        List<Alerta> alertas = new ArrayList<>();

        if (imoveisUsuario.isEmpty()) {
            alertas.add(new Alerta(
                    "sem-imoveis",
                    SeveridadeAlerta.INFO,
                    "Nenhum imovel cadastrado",
                    "Cadastre seu primeiro imovel para comecar a acompanhar o patrimonio."));
        }

        for (ContratoEntity contrato : contratosUsuario) {
            if (contrato.getStatus() != StatusContrato.ATIVO) {
                continue;
            }
            String nomeImovel = contrato.getImovel() != null ? contrato.getImovel().getNome() : "Imovel";

            LocalDate fim = contrato.getDataFim();
            if (fim != null) {
                if (fim.isBefore(hoje)) {
                    alertas.add(new Alerta(
                            "contrato-vencido-" + contrato.getId(),
                            SeveridadeAlerta.DANGER,
                            "Contrato vencido - " + nomeImovel,
                            "O contrato encerrou em " + fim.format(DATA) + " e ainda consta como ativo."));
                } else if (!fim.isAfter(em60Dias)) {
                    alertas.add(new Alerta(
                            "contrato-vence-" + contrato.getId(),
                            SeveridadeAlerta.WARNING,
                            "Contrato proximo do vencimento - " + nomeImovel,
                            "Vence em " + fim.format(DATA) + ". Avalie renovacao ou reajuste."));
                }
            }

            Reajuste reajuste = calcularReajuste(contrato, hoje);
            if (reajuste != null) {
                String indice = contrato.getIndiceReajuste() != null
                        ? " pelo indice " + contrato.getIndiceReajuste().name()
                        : "";
                if (reajuste.noMesAtual()) {
                    alertas.add(new Alerta(
                            "reajuste-" + contrato.getId(),
                            SeveridadeAlerta.WARNING,
                            "Reajuste do aluguel - " + nomeImovel,
                            "O contrato completa " + reajuste.meses() + " meses de vigencia em "
                                    + reajuste.data().format(DATA) + ". Aplique o reajuste" + indice + "."));
                } else if (!reajuste.data().isAfter(em30Dias)) {
                    alertas.add(new Alerta(
                            "reajuste-" + contrato.getId(),
                            SeveridadeAlerta.INFO,
                            "Reajuste previsto - " + nomeImovel,
                            "O contrato completara " + reajuste.meses() + " meses de vigencia em "
                                    + reajuste.data().format(DATA) + ". Reajuste" + indice + "."));
                }
            }
        }

        Set<UUID> idsImoveisComContratoAtivo = contratosUsuario.stream()
                .filter(c -> c.getStatus() == StatusContrato.ATIVO && c.getImovel() != null)
                .map(c -> c.getImovel().getId())
                .collect(Collectors.toSet());

        for (ImovelEntity imovel : imoveisUsuario) {
            if (imovel.getStatus() == StatusImovel.DISPONIVEL) {
                alertas.add(new Alerta(
                        "imovel-disponivel-" + imovel.getId(),
                        SeveridadeAlerta.WARNING,
                        "Imovel disponivel - " + imovel.getNome(),
                        "Sem contrato ativo. Potencial de receita nao aproveitado."));
            } else if (imovel.getStatus() == StatusImovel.ALUGADO
                    && !idsImoveisComContratoAtivo.contains(imovel.getId())) {
                alertas.add(new Alerta(
                        "imovel-inconsistente-" + imovel.getId(),
                        SeveridadeAlerta.DANGER,
                        "Inconsistencia - " + imovel.getNome(),
                        "Marcado como alugado, mas nao ha contrato ativo vinculado."));
            }
        }

        alertas.sort(Comparator.comparingInt(a -> switch (a.severidade()) {
            case DANGER -> 0;
            case WARNING -> 1;
            case INFO -> 2;
        }));
        return alertas;
    }

    /**
     * Contratos ativos cujo reajuste cai no mes corrente (completam 12, 24, 36...
     * meses de vigencia, conforme o periodo de reajuste). Usado pelo push de reajuste.
     */
    public List<ReajusteDoMes> listarReajustesDoMes(LocalDate hoje) {
        List<ReajusteDoMes> resultado = new ArrayList<>();
        for (ContratoEntity contrato : contratoRepository.findAll()) {
            if (contrato.getStatus() != StatusContrato.ATIVO || contrato.getImovel() == null
                    || contrato.getImovel().getUsuario() == null) {
                continue;
            }
            Reajuste reajuste = calcularReajuste(contrato, hoje);
            if (reajuste != null && reajuste.noMesAtual()) {
                resultado.add(new ReajusteDoMes(
                        contrato.getId(),
                        contrato.getImovel().getUsuario().getId(),
                        contrato.getImovel().getNome(),
                        contrato.getIndiceReajuste(),
                        reajuste.data(),
                        reajuste.meses()));
            }
        }
        return resultado;
    }

    /**
     * Reajuste a cada {@code periodoReajuste} meses contados de {@code dataInicio}:
     * o do mes corrente (se o contrato completa um multiplo do periodo neste mes)
     * ou, senao, o proximo. {@code null} se o contrato nao reajusta ou se o
     * proximo reajuste cai depois do fim do contrato.
     */
    static Reajuste calcularReajuste(ContratoEntity contrato, LocalDate hoje) {
        LocalDate inicio = contrato.getDataInicio();
        Integer periodo = contrato.getPeriodoReajuste();
        if (inicio == null || periodo == null || periodo <= 0
                || contrato.getIndiceReajuste() == IndiceReajuste.SEM_REAJUSTE) {
            return null;
        }
        long decorridos = ChronoUnit.MONTHS.between(YearMonth.from(inicio), YearMonth.from(hoje));
        boolean noMesAtual = decorridos > 0 && decorridos % periodo == 0;
        long meses = noMesAtual ? decorridos : (Math.max(decorridos, 0) / periodo + 1) * periodo;
        LocalDate data = inicio.plusMonths(meses); // plusMonths ja limita ao ultimo dia do mes
        if (contrato.getDataFim() != null && data.isAfter(contrato.getDataFim())) {
            return null;
        }
        return new Reajuste(data, meses, noMesAtual);
    }

    record Reajuste(LocalDate data, long meses, boolean noMesAtual) {
    }

    public record ReajusteDoMes(
            UUID contratoId,
            UUID usuarioId,
            String nomeImovel,
            IndiceReajuste indice,
            LocalDate data,
            long meses) {
    }
}
