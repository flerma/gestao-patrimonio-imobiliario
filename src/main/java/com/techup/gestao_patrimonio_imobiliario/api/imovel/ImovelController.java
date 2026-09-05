package com.techup.gestao_patrimonio_imobiliario.api.imovel;

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

import com.techup.gestao_patrimonio_imobiliario.core.imovel.Imovel;
import com.techup.gestao_patrimonio_imobiliario.core.imovel.ImovelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/imoveis")
@RequiredArgsConstructor
@Tag(name = "Imóveis", description = "Cadastro e manutenção de imóveis do patrimônio")
public class ImovelController {

    private final ImovelService imovelService;

    @PostMapping
    @Operation(summary = "Criar imóvel", description = "Cadastra um novo imóvel e retorna o recurso criado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imóvel criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<ImovelResponse> criar(@Valid @RequestBody ImovelRequest request) {
        Imovel imovel = imovelService.criar(request);
        return ResponseEntity.created(URI.create("/api/imoveis/" + imovel.getId())).body(ImovelResponse.from(imovel));
    }

    @GetMapping
    @Operation(summary = "Listar imóveis", description = "Retorna todos os imóveis cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<ImovelResponse> listar() {
        return imovelService.listar().stream()
                .map(ImovelResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar imóvel por ID", description = "Retorna um imóvel a partir do seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imóvel encontrado"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado", content = @Content)
    })
    public ImovelResponse buscarPorId(
            @Parameter(description = "Identificador do imóvel", required = true) @PathVariable UUID id) {
        return ImovelResponse.from(imovelService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar imóvel", description = "Atualiza os dados de um imóvel existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imóvel atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado", content = @Content)
    })
    public ImovelResponse atualizar(
            @Parameter(description = "Identificador do imóvel", required = true) @PathVariable UUID id,
            @Valid @RequestBody ImovelRequest request) {
        return ImovelResponse.from(imovelService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir imóvel", description = "Remove um imóvel existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imóvel excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Imóvel não encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do imóvel", required = true) @PathVariable UUID id) {
        imovelService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
