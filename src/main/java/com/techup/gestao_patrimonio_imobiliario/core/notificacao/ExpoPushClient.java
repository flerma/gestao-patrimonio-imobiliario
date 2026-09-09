package com.techup.gestao_patrimonio_imobiliario.core.notificacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Cliente minimo do servico de push da Expo (https://exp.host/--/api/v2/push/send).
 * Envia em lotes e devolve os "tickets" para que o chamador desative tokens mortos.
 */
@Component
public class ExpoPushClient {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushClient.class);
    private static final int TAMANHO_LOTE = 100;

    private final RestClient rest;

    public ExpoPushClient(
            @Value("${integracao.expo.push-url:https://exp.host/--/api/v2/push/send}") String pushUrl,
            @Value("${integracao.expo.access-token:}") String accessToken) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(pushUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (accessToken != null && !accessToken.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        this.rest = builder.build();
    }

    /** Envia as mensagens; retorna os tickets na mesma ordem (pode vir menor em caso de falha de rede). */
    public List<Ticket> enviar(List<Mensagem> mensagens) {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < mensagens.size(); i += TAMANHO_LOTE) {
            List<Mensagem> lote = mensagens.subList(i, Math.min(i + TAMANHO_LOTE, mensagens.size()));
            try {
                Resposta resposta = rest.post().body(lote).retrieve().body(Resposta.class);
                if (resposta != null && resposta.data() != null) {
                    tickets.addAll(resposta.data());
                }
            } catch (RuntimeException ex) {
                log.warn("Falha ao enviar lote de push para a Expo ({} mensagens): {}", lote.size(), ex.getMessage());
            }
        }
        return tickets;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Mensagem(
            String to,
            String title,
            String body,
            Map<String, Object> data,
            String sound,
            String channelId,
            String priority) {

        public static Mensagem alerta(String to, String title, String body, Map<String, Object> data) {
            return new Mensagem(to, title, body, data, "default", "alertas", "high");
        }
    }

    public record Resposta(List<Ticket> data) {
    }

    public record Ticket(String status, String id, String message, Detalhes details) {

        public boolean falhou() {
            return "error".equalsIgnoreCase(status);
        }

        public boolean dispositivoInvalido() {
            return details != null && "DeviceNotRegistered".equals(details.error());
        }
    }

    public record Detalhes(String error) {
    }
}
