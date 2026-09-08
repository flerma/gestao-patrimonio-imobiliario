package com.techup.gestao_patrimonio_imobiliario.api.pagamentoaluguel;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel.PagamentoAluguel;
import com.techup.gestao_patrimonio_imobiliario.core.pagamentoaluguel.PagamentoAluguelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagamentos-aluguel")
@RequiredArgsConstructor
@Tag(name = "Pagamentos de Aluguel", description = "Cobranca e baixa dos alugueis gerados a partir dos contratos")
public class PagamentoAluguelController {

    private final PagamentoAluguelService pagamentoAluguelService;

    @PostMapping
    @Operation(summary = "Criar pagamento de aluguel",
            description = "Cadastra manualmente uma cobranca de aluguel. A geracao automatica ocorre ao criar o contrato.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Contrato nao encontrado", content = @Content)
    })
    public ResponseEntity<PagamentoAluguelResponse> criar(@Valid @RequestBody PagamentoAluguelRequest request) {
        PagamentoAluguel pagamento = pagamentoAluguelService.criar(request);
        return ResponseEntity.created(URI.create("/api/pagamentos-aluguel/" + pagamento.getId()))
                .body(PagamentoAluguelResponse.from(pagamento));
    }

    @GetMapping
    @Operation(summary = "Listar pagamentos de aluguel",
            description = "Retorna os pagamentos, opcionalmente filtrados por contrato.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<PagamentoAluguelResponse> listar(
            @Parameter(description = "Filtra os pagamentos de um contrato especifico")
            @RequestParam(name = "contratoId", required = false) UUID contratoId) {
        return pagamentoAluguelService.listar(contratoId).stream()
                .map(PagamentoAluguelResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID", description = "Retorna um pagamento de aluguel pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento nao encontrado", content = @Content)
    })
    public PagamentoAluguelResponse buscarPorId(
            @Parameter(description = "Identificador do pagamento", required = true) @PathVariable UUID id) {
        return PagamentoAluguelResponse.from(pagamentoAluguelService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pagamento", description = "Atualiza os dados de um pagamento de aluguel existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pagamento nao encontrado", content = @Content)
    })
    public PagamentoAluguelResponse atualizar(
            @Parameter(description = "Identificador do pagamento", required = true) @PathVariable UUID id,
            @Valid @RequestBody PagamentoAluguelRequest request) {
        return PagamentoAluguelResponse.from(pagamentoAluguelService.atualizar(id, request));
    }

    @PostMapping("/{id}/registrar-pagamento")
    @Operation(summary = "Registrar pagamento",
            description = "Registra o recebimento e recalcula o status (PAGO, PAGO_COM_ATRASO ou PAGO_PARCIALMENTE).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pagamento nao encontrado", content = @Content)
    })
    public PagamentoAluguelResponse registrarPagamento(
            @Parameter(description = "Identificador do pagamento", required = true) @PathVariable UUID id,
            @Valid @RequestBody RegistrarPagamentoRequest request) {
        return PagamentoAluguelResponse.from(pagamentoAluguelService.registrarPagamento(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pagamento", description = "Remove um pagamento de aluguel existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pagamento excluido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pagamento nao encontrado", content = @Content)
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "Identificador do pagamento", required = true) @PathVariable UUID id) {
        pagamentoAluguelService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
