package com.techup.gestao_patrimonio_imobiliario.core.notificacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.notificacao.RegistrarDispositivoRequest;
import com.techup.gestao_patrimonio_imobiliario.core.alerta.Alerta;
import com.techup.gestao_patrimonio_imobiliario.core.alerta.AlertaService;
import com.techup.gestao_patrimonio_imobiliario.core.alerta.AlertaService.ReajusteDoMes;
import com.techup.gestao_patrimonio_imobiliario.core.alerta.SeveridadeAlerta;
import com.techup.gestao_patrimonio_imobiliario.core.notificacao.ExpoPushClient.Mensagem;
import com.techup.gestao_patrimonio_imobiliario.core.notificacao.ExpoPushClient.Ticket;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.DispositivoPushEntity;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.DispositivoPushRepository;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.NotificacaoEnviadaEntity;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.NotificacaoEnviadaRepository;

@Service
@Transactional
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private static final String TITULO = "Patrimonio Imobiliario";

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DispositivoPushRepository dispositivoPushRepository;
    private final NotificacaoEnviadaRepository notificacaoEnviadaRepository;
    private final AlertaService alertaService;
    private final ExpoPushClient expoPushClient;

    public NotificacaoService(DispositivoPushRepository dispositivoPushRepository,
                              NotificacaoEnviadaRepository notificacaoEnviadaRepository,
                              AlertaService alertaService,
                              ExpoPushClient expoPushClient) {
        this.dispositivoPushRepository = dispositivoPushRepository;
        this.notificacaoEnviadaRepository = notificacaoEnviadaRepository;
        this.alertaService = alertaService;
        this.expoPushClient = expoPushClient;
    }

    public DispositivoPushEntity registrarDispositivo(RegistrarDispositivoRequest request) {
        LocalDateTime agora = LocalDateTime.now();
        DispositivoPushEntity dispositivo = dispositivoPushRepository
                .findByExpoPushToken(request.getExpoPushToken())
                .orElseGet(() -> DispositivoPushEntity.builder()
                        .id(UUID.randomUUID())
                        .expoPushToken(request.getExpoPushToken())
                        .dataCriacao(agora)
                        .build());
        dispositivo.setUsuarioId(request.getUsuarioId());
        dispositivo.setPlataforma(request.getPlataforma());
        dispositivo.setAtivo(true);
        dispositivo.setDataAtualizacao(agora);
        return dispositivoPushRepository.save(dispositivo);
    }

    public void removerDispositivo(String expoPushToken) {
        dispositivoPushRepository.findByExpoPushToken(expoPushToken)
                .ifPresent(dispositivoPushRepository::delete);
    }

    /** Envia um push de teste para um token ja registrado. */
    public ResultadoEnvio enviarTeste(String expoPushToken) {
        DispositivoPushEntity dispositivo = dispositivoPushRepository.findByExpoPushToken(expoPushToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Dispositivo nao registrado: " + expoPushToken));
        Mensagem mensagem = Mensagem.alerta(
                dispositivo.getExpoPushToken(),
                "Teste de notificacao",
                "Se voce recebeu isso, as notificacoes estao configuradas.",
                Map.of("tipo", "teste"));
        List<Ticket> tickets = expoPushClient.enviar(List.of(mensagem));
        int falhas = tratarTickets(List.of(mensagem), tickets);
        return new ResultadoEnvio(1, 1 - falhas, falhas, List.of());
    }

    /**
     * Reavalia os alertas por proprietario e envia um resumo para cada aparelho
     * registrado. Chamado pelo agendador diario e pelo endpoint manual.
     */
    public ResultadoEnvio enviarResumoDeAlertas() {
        List<DispositivoPushEntity> dispositivos = dispositivoPushRepository.findByAtivoTrue();
        if (dispositivos.isEmpty()) {
            return new ResultadoEnvio(0, 0, 0, List.of("Nenhum dispositivo registrado."));
        }

        Map<UUID, List<DispositivoPushEntity>> porUsuario = new HashMap<>();
        for (DispositivoPushEntity d : dispositivos) {
            porUsuario.computeIfAbsent(d.getUsuarioId(), k -> new ArrayList<>()).add(d);
        }

        List<Mensagem> mensagens = new ArrayList<>();
        List<String> detalhes = new ArrayList<>();
        for (Map.Entry<UUID, List<DispositivoPushEntity>> entrada : porUsuario.entrySet()) {
            UUID usuarioId = entrada.getKey();
            List<Alerta> alertas = alertaService.listarParaUsuario(usuarioId);
            if (alertas.isEmpty()) {
                detalhes.add((usuarioId == null ? "consolidado" : usuarioId) + ": sem alertas");
                continue;
            }
            String corpo = resumo(alertas);
            Map<String, Object> data = new HashMap<>();
            data.put("tipo", "resumo-alertas");
            data.put("quantidade", alertas.size());
            if (usuarioId != null) {
                data.put("usuarioId", usuarioId.toString());
            }
            for (DispositivoPushEntity d : entrada.getValue()) {
                mensagens.add(Mensagem.alerta(d.getExpoPushToken(), TITULO, corpo, data));
            }
            detalhes.add((usuarioId == null ? "consolidado" : usuarioId)
                    + ": " + alertas.size() + " alerta(s) -> " + entrada.getValue().size() + " aparelho(s)");
        }

        if (mensagens.isEmpty()) {
            return new ResultadoEnvio(dispositivos.size(), 0, 0, detalhes);
        }
        List<Ticket> tickets = expoPushClient.enviar(mensagens);
        int falhas = tratarTickets(mensagens, tickets);
        return new ResultadoEnvio(dispositivos.size(), mensagens.size() - falhas, falhas, detalhes);
    }

    /**
     * Push dedicado para cada contrato que atinge o periodo de reajuste no mes
     * corrente (completa 12, 24, 36... meses de vigencia). Enviado uma unica vez
     * por contrato e ciclo: o envio so e registrado quando ao menos um aparelho
     * do proprietario recebe, assim quem ativar as notificacoes depois ainda
     * recebe o aviso dentro do mes.
     */
    public ResultadoEnvio enviarAvisosDeReajuste() {
        LocalDate hoje = LocalDate.now(ZONA);
        List<ReajusteDoMes> reajustes = alertaService.listarReajustesDoMes(hoje);
        if (reajustes.isEmpty()) {
            return new ResultadoEnvio(0, 0, 0, List.of("Nenhum reajuste no mes."));
        }

        Map<UUID, List<DispositivoPushEntity>> porUsuario = new HashMap<>();
        for (DispositivoPushEntity d : dispositivoPushRepository.findByAtivoTrue()) {
            if (d.getUsuarioId() != null) {
                porUsuario.computeIfAbsent(d.getUsuarioId(), k -> new ArrayList<>()).add(d);
            }
        }

        int dispositivos = 0;
        int enviadas = 0;
        int falhas = 0;
        List<String> detalhes = new ArrayList<>();
        for (ReajusteDoMes reajuste : reajustes) {
            String chave = "reajuste-" + reajuste.contratoId() + "-" + YearMonth.from(reajuste.data());
            if (notificacaoEnviadaRepository.existsByChave(chave)) {
                continue;
            }
            List<DispositivoPushEntity> aparelhos = porUsuario.getOrDefault(reajuste.usuarioId(), List.of());
            if (aparelhos.isEmpty()) {
                detalhes.add(chave + ": proprietario sem aparelho registrado");
                continue;
            }
            String indice = reajuste.indice() != null ? " pelo indice " + reajuste.indice().name() : "";
            String corpo = reajuste.nomeImovel() + ": o contrato completa " + reajuste.meses()
                    + " meses de vigencia em " + reajuste.data().format(DATA)
                    + ". Aplique o reajuste" + indice + ".";
            Map<String, Object> data = new HashMap<>();
            data.put("tipo", "reajuste");
            data.put("contratoId", reajuste.contratoId().toString());
            data.put("href", "/contratos/" + reajuste.contratoId());

            List<Mensagem> mensagens = aparelhos.stream()
                    .map(d -> Mensagem.alerta(d.getExpoPushToken(), "Reajuste do aluguel", corpo, data))
                    .toList();
            List<Ticket> tickets = expoPushClient.enviar(mensagens);
            int falhasContrato = tratarTickets(mensagens, tickets);
            dispositivos += aparelhos.size();
            enviadas += mensagens.size() - falhasContrato;
            falhas += falhasContrato;
            if (falhasContrato < mensagens.size()) {
                notificacaoEnviadaRepository.save(NotificacaoEnviadaEntity.builder()
                        .id(UUID.randomUUID())
                        .chave(chave)
                        .usuarioId(reajuste.usuarioId())
                        .dataEnvio(LocalDateTime.now())
                        .build());
            }
            detalhes.add(chave + ": " + (mensagens.size() - falhasContrato) + "/" + mensagens.size() + " enviada(s)");
        }
        return new ResultadoEnvio(dispositivos, enviadas, falhas, detalhes);
    }

    private String resumo(List<Alerta> alertas) {
        long criticos = alertas.stream().filter(a -> a.severidade() == SeveridadeAlerta.DANGER).count();
        long avisos = alertas.stream().filter(a -> a.severidade() == SeveridadeAlerta.WARNING).count();
        StringBuilder sb = new StringBuilder();
        sb.append(alertas.size()).append(alertas.size() == 1 ? " alerta" : " alertas");
        List<String> partes = new ArrayList<>();
        if (criticos > 0) {
            partes.add(criticos + (criticos == 1 ? " critico" : " criticos"));
        }
        if (avisos > 0) {
            partes.add(avisos + (avisos == 1 ? " aviso" : " avisos"));
        }
        if (!partes.isEmpty()) {
            sb.append(" (").append(String.join(", ", partes)).append(")");
        }
        sb.append(": ").append(alertas.get(0).titulo());
        return sb.toString();
    }

    /** Desativa tokens recusados pela Expo (DeviceNotRegistered). Retorna o total de falhas. */
    private int tratarTickets(List<Mensagem> mensagens, List<Ticket> tickets) {
        int falhas = 0;
        for (int i = 0; i < tickets.size() && i < mensagens.size(); i++) {
            Ticket ticket = tickets.get(i);
            if (!ticket.falhou()) {
                continue;
            }
            falhas++;
            String token = mensagens.get(i).to();
            log.warn("Push recusado para {}: {}", token, ticket.message());
            if (ticket.dispositivoInvalido()) {
                dispositivoPushRepository.findByExpoPushToken(token).ifPresent(d -> {
                    d.setAtivo(false);
                    d.setDataAtualizacao(LocalDateTime.now());
                    dispositivoPushRepository.save(d);
                });
            }
        }
        // tickets ausentes (falha de rede) contam como falha
        falhas += Math.max(0, mensagens.size() - tickets.size());
        return falhas;
    }

    public record ResultadoEnvio(
            int dispositivos,
            int mensagensEnviadas,
            int falhas,
            List<String> detalhes) {
    }
}
