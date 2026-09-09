package com.techup.gestao_patrimonio_imobiliario.core.alerta;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

            LocalDate inicio = contrato.getDataInicio();
            Integer periodo = contrato.getPeriodoReajuste();
            IndiceReajuste indice = contrato.getIndiceReajuste();
            if (inicio != null && periodo != null && periodo > 0
                    && indice != null && indice != IndiceReajuste.SEM_REAJUSTE) {
                LocalDate proximoReajuste = inicio;
                while (!proximoReajuste.isAfter(hoje)) {
                    proximoReajuste = proximoReajuste.plusMonths(periodo);
                }
                if (!proximoReajuste.isAfter(em30Dias)) {
                    alertas.add(new Alerta(
                            "reajuste-" + contrato.getId(),
                            SeveridadeAlerta.INFO,
                            "Reajuste previsto - " + nomeImovel,
                            "Reajuste (" + indice.name() + ") em " + proximoReajuste.format(DATA) + "."));
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
}
