package com.techup.gestao_patrimonio_imobiliario.api.endereco;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techup.gestao_patrimonio_imobiliario.core.endereco.ConsultaCepService;
import com.techup.gestao_patrimonio_imobiliario.core.endereco.ConsultaMunicipioService;
import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Consulta de endereço por CEP (provedor ViaCEP) e municípios por UF (provedor IBGE)")
public class EnderecoController {

    private final ConsultaCepService consultaCepService;
    private final ConsultaMunicipioService consultaMunicipioService;

    @GetMapping("/cep/{cep}")
    @Operation(summary = "Consultar endereço por CEP",
            description = "Consulta o provedor ViaCEP e retorna o endereço correspondente ao CEP. "
                    + "Aceita o CEP com ou sem máscara (ex.: 01001000 ou 01001-000).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
            @ApiResponse(responseCode = "400", description = "CEP inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "CEP não encontrado", content = @Content),
            @ApiResponse(responseCode = "502", description = "Provedor de CEP indisponível", content = @Content)
    })
    public Endereco consultarPorCep(
            @Parameter(description = "CEP com 8 dígitos", required = true, example = "01001-000")
            @PathVariable String cep) {
        return consultaCepService.buscarPorCep(cep);
    }

    @GetMapping("/municipios/{uf}")
    @Operation(summary = "Listar municípios de uma UF",
            description = "Consulta o provedor do IBGE e retorna os nomes dos municípios da UF informada, "
                    + "em ordem alfabética.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "UF inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "UF não encontrada", content = @Content),
            @ApiResponse(responseCode = "502", description = "Provedor de municípios indisponível", content = @Content)
    })
    public List<String> listarMunicipiosPorUf(
            @Parameter(description = "Sigla da UF", required = true, example = "SP")
            @PathVariable String uf) {
        return consultaMunicipioService.listarPorUf(uf);
    }
}
