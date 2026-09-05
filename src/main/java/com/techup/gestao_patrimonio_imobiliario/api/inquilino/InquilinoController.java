package com.techup.gestao_patrimonio_imobiliario.api.inquilino;

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

import com.techup.gestao_patrimonio_imobiliario.core.inquilino.Inquilino;
import com.techup.gestao_patrimonio_imobiliario.core.inquilino.InquilinoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inquilinos")
@RequiredArgsConstructor
@Tag(name = "Inquilinos", description = "Cadastro e manutenção de inquilinos")
public class InquilinoController {

    private final InquilinoService inquilinoService;

    @PostMapping
    @Operation(summary = "Criar inquilino", description = "Cadastra um novo inquilino e retorna o recurso criado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inquilino criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<InquilinoResponse> criar(@Valid @RequestBody InquilinoRequest request) {
        Inquilino inquilino = inquilinoService.criar(request);
        return ResponseEntity.created(URI.create("/api/inquilinos/" + inquilino.getId())).body(InquilinoResponse.from(inquilino));
    }

    @GetMapping
    @Operation(summary = "Listar inquilinos", description = "Retorna todos os inquilinos cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<InquilinoResponse> listar() {
        return inquilinoService.listar().stream()
                .map(InquilinoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar inquilino por ID", description = "Retorna um inquilino a partir do seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inquilino encontrado"),
            @ApiResponse(responseCode = "404", description = "Inquilino não encontrado", content = @Content)
    })
    public InquilinoResponse buscarPorId(
            @Parameter(description = "Identificador do inquilino", required = true) @PathVariable UUID id) {
        return InquilinoResponse.from(inquilinoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar inquilino", description = "Atualiza os dados de um inquilino existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inquilino atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Inquilino não encontrado", content = @Content)
    })
    public InquilinoResponse atualizar(
            @Parameter(description = "Identificador do inquilino", required = true) @PathVariable UUID id,
            @Valid @RequestBody InquilinoRequest request) {
        return InquilinoResponse.from(inquilinoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir inquilino", description = "Remove um inquilino existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inquilino excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inquilino não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do inquilino", required = true) @PathVariable UUID id) {
        inquilinoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
