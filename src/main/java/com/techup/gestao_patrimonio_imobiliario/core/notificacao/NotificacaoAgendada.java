package com.techup.gestao_patrimonio_imobiliario.core.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.techup.gestao_patrimonio_imobiliario.core.notificacao.NotificacaoService.ResultadoEnvio;

/**
 * Envia o resumo diario de alertas por push. So dispara enquanto a aplicacao
 * estiver no ar. Ajuste o horario com {@code notificacao.resumo-diario.cron}.
 */
@Component
public class NotificacaoAgendada {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoAgendada.class);

    private final NotificacaoService notificacaoService;

    public NotificacaoAgendada(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @Scheduled(cron = "${notificacao.resumo-diario.cron:0 0 8 * * *}", zone = "America/Sao_Paulo")
    public void resumoDiario() {
        ResultadoEnvio resultado = notificacaoService.enviarResumoDeAlertas();
        log.info("Resumo diario de alertas: {} aparelho(s), {} enviada(s), {} falha(s)",
                resultado.dispositivos(), resultado.mensagensEnviadas(), resultado.falhas());

        try {
            ResultadoEnvio reajustes = notificacaoService.enviarAvisosDeReajuste();
            log.info("Avisos de reajuste: {} aparelho(s), {} enviada(s), {} falha(s) {}",
                    reajustes.dispositivos(), reajustes.mensagensEnviadas(), reajustes.falhas(),
                    reajustes.detalhes());
        } catch (RuntimeException e) {
            log.error("Falha ao enviar avisos de reajuste", e);
        }
    }
}
