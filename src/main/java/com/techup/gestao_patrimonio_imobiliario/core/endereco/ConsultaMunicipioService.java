package com.techup.gestao_patrimonio_imobiliario.core.endereco;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.data.ibge.IbgeClient;
import com.techup.gestao_patrimonio_imobiliario.data.ibge.MunicipioIbgeResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consulta de municípios por UF usando a API de localidades do IBGE (via Feign).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaMunicipioService {

    private final IbgeClient ibgeClient;

    public List<String> listarPorUf(String ufInformado) {
        String uf = normalizar(ufInformado);

        List<MunicipioIbgeResponse> resposta;
        try {
            resposta = ibgeClient.buscarMunicipiosPorUf(uf);
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF inválida: " + ufInformado);
        } catch (FeignException e) {
            log.warn("Falha ao consultar municípios do IBGE para a UF {}: {}", uf, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Serviço de consulta de municípios indisponível. Tente novamente.");
        }

        if (resposta == null || resposta.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UF não encontrada: " + ufInformado);
        }

        Collator collator = Collator.getInstance(new Locale("pt", "BR"));
        return resposta.stream()
                .map(MunicipioIbgeResponse::getNome)
                .filter(nome -> nome != null && !nome.isBlank())
                .sorted(collator)
                .toList();
    }

    private String normalizar(String uf) {
        if (uf == null || uf.trim().length() != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF deve conter 2 letras: " + uf);
        }
        return uf.trim().toUpperCase();
    }
}
