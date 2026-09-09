package com.techup.gestao_patrimonio_imobiliario.core.notificacao;

import java.time.LocalDateTime;
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
import com.techup.gestao_patrimonio_imobiliario.core.alerta.SeveridadeAlerta;
import com.techup.gestao_patrimonio_imobiliario.core.notificacao.ExpoPushClient.Mensagem;
import com.techup.gestao_patrimonio_imobiliario.core.notificacao.ExpoPushClient.Ticket;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.DispositivoPushEntity;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.DispositivoPushRepository;

@Service
@Transactional
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private static final String TITULO = "Patrimonio Imobiliario";

    private final DispositivoPushRepository dispositivoPushRepository;
    private final AlertaService alertaService;
    private final ExpoPushClient expoPushClient;

    public NotificacaoService(DispositivoPushRepository dispositivoPushRepository,
                              AlertaService alertaService,
                              ExpoPushClient expoPushClient) {
        this.dispositivoPushRepository = dispositivoPushRepository;
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
