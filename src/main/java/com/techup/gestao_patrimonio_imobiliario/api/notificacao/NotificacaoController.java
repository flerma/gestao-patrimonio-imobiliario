package com.techup.gestao_patrimonio_imobiliario.api.notificacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techup.gestao_patrimonio_imobiliario.core.notificacao.NotificacaoService;
import com.techup.gestao_patrimonio_imobiliario.core.notificacao.NotificacaoService.ResultadoEnvio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificacoes", description = "Registro de aparelhos e envio de notificacoes push (Expo)")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PostMapping("/dispositivos")
    @Operation(summary = "Registrar aparelho",
            description = "Cadastra/atualiza o token de push Expo do aparelho e o proprietario selecionado.")
    public DispositivoPushResponse registrar(@Valid @RequestBody RegistrarDispositivoRequest request) {
        return DispositivoPushResponse.from(notificacaoService.registrarDispositivo(request));
    }

    @DeleteMapping("/dispositivos")
    @Operation(summary = "Remover aparelho", description = "Remove o registro de um token de push.")
    public ResponseEntity<Void> remover(@RequestParam("token") String token) {
        notificacaoService.removerDispositivo(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enviar")
    @Operation(summary = "Enviar resumo de alertas agora",
            description = "Reavalia os alertas e envia o resumo para todos os aparelhos registrados (uso manual/teste).")
    public ResultadoEnvio enviar() {
        return notificacaoService.enviarResumoDeAlertas();
    }

    @PostMapping("/testar")
    @Operation(summary = "Enviar push de teste", description = "Envia uma notificacao fixa de teste para um token registrado.")
    public ResultadoEnvio testar(@RequestParam("token") String token) {
        return notificacaoService.enviarTeste(token);
    }
}
