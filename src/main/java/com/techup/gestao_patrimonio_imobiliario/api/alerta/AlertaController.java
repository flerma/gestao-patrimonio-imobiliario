package com.techup.gestao_patrimonio_imobiliario.api.alerta;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techup.gestao_patrimonio_imobiliario.core.alerta.AlertaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Alertas operacionais derivados de imoveis e contratos")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Listar alertas",
            description = "Reavalia as regras de alerta do dashboard. Sem 'usuarioId' retorna o consolidado.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<AlertaResponse> listar(
            @Parameter(description = "Filtra pelos imoveis de um proprietario")
            @RequestParam(name = "usuarioId", required = false) UUID usuarioId) {
        return alertaService.listarParaUsuario(usuarioId).stream()
                .map(AlertaResponse::from)
                .toList();
    }
}
