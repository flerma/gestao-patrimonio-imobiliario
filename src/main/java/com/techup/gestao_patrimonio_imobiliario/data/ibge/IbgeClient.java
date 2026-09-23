package com.techup.gestao_patrimonio_imobiliario.data.ibge;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign para a API de localidades do IBGE
 * (https://servicodados.ibge.gov.br). Consulta de municípios por UF.
 */
@FeignClient(
        name = "ibgeClient",
        url = "${integracao.ibge.url:https://servicodados.ibge.gov.br/api/v1/localidades}")
public interface IbgeClient {

    @GetMapping(value = "/estados/{uf}/municipios", headers = "Accept=application/json")
    List<MunicipioIbgeResponse> buscarMunicipiosPorUf(@PathVariable("uf") String uf);
}
