package com.techup.gestao_patrimonio_imobiliario.api.contrato;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techup.gestao_patrimonio_imobiliario.core.contrato.Contrato;
import com.techup.gestao_patrimonio_imobiliario.core.contrato.ContratoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
@Tag(name = "Contratos", description = "Cadastro e manutenção de contratos de locação")
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    @Operation(summary = "Criar contrato", description = "Cadastra um novo contrato de locação e retorna o recurso criado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrato criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<ContratoResponse> criar(@Valid @RequestBody ContratoRequest request) {
        Contrato contrato = contratoService.criar(request);
        return ResponseEntity.created(URI.create("/api/contratos/" + contrato.getId())).body(ContratoResponse.from(contrato));
    }

    @GetMapping
    @Operation(summary = "Listar contratos", description = "Retorna todos os contratos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<ContratoResponse> listar() {
        return contratoService.listar().stream()
                .map(ContratoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar contrato por ID", description = "Retorna um contrato a partir do seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
            @ApiResponse(responseCode = "404", description = "Contrato não encontrado", content = @Content)
    })
    public ContratoResponse buscarPorId(
            @Parameter(description = "Identificador do contrato", required = true) @PathVariable UUID id) {
        return ContratoResponse.from(contratoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar contrato", description = "Atualiza os dados de um contrato existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrato atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Contrato não encontrado", content = @Content)
    })
    public ContratoResponse atualizar(
            @Parameter(description = "Identificador do contrato", required = true) @PathVariable UUID id,
            @Valid @RequestBody ContratoRequest request) {
        return ContratoResponse.from(contratoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir contrato", description = "Remove um contrato existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contrato excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Contrato não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do contrato", required = true) @PathVariable UUID id) {
        contratoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
