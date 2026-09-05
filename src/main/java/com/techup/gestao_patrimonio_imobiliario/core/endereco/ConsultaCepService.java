package com.techup.gestao_patrimonio_imobiliario.core.endereco;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.data.viacep.ViaCepClient;
import com.techup.gestao_patrimonio_imobiliario.data.viacep.ViaCepResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consulta de endereço por CEP usando o provedor externo ViaCEP (via Feign).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaCepService {

    private final ViaCepClient viaCepClient;

    public Endereco buscarPorCep(String cepInformado) {
        String cep = normalizar(cepInformado);

        ViaCepResponse resposta;
        try {
            resposta = viaCepClient.buscarPorCep(cep);
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "CEP inválido: " + cepInformado);
        } catch (FeignException e) {
            log.warn("Falha ao consultar ViaCEP para o CEP {}: {}", cep, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Serviço de consulta de CEP indisponível. Tente novamente.");
        }

        if (resposta == null || resposta.isErro()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "CEP não encontrado: " + cepInformado);
        }

        return Endereco.builder()
                .cep(formatar(cep))
                .logradouro(vazioParaNulo(resposta.getLogradouro()))
                .complemento(vazioParaNulo(resposta.getComplemento()))
                .bairro(vazioParaNulo(resposta.getBairro()))
                .cidade(vazioParaNulo(resposta.getLocalidade()))
                .estado(vazioParaNulo(resposta.getUf()))
                .pais("Brasil")
                .build();
    }

    private String normalizar(String cep) {
        String somenteDigitos = cep == null ? "" : cep.replaceAll("\\D", "");
        if (somenteDigitos.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "CEP deve conter 8 dígitos: " + cep);
        }
        return somenteDigitos;
    }

    private String formatar(String cepOitoDigitos) {
        return cepOitoDigitos.substring(0, 5) + "-" + cepOitoDigitos.substring(5);
    }

    private String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
