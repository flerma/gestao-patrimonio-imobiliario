package com.techup.gestao_patrimonio_imobiliario.data.viacep;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para o serviço público ViaCEP (https://viacep.com.br).
 * Consulta de endereço a partir do CEP.
 */
@FeignClient(
        name = "viaCepClient",
        url = "${integracao.viacep.url:https://viacep.com.br/ws}")
public interface ViaCepClient {

    @GetMapping(value = "/{cep}/json/", headers = "Accept=application/json")
    ViaCepResponse buscarPorCep(@PathVariable("cep") String cep);
}
