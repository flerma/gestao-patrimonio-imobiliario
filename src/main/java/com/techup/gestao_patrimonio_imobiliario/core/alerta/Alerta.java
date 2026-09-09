package com.techup.gestao_patrimonio_imobiliario.core.alerta;

/**
 * Alerta operacional derivado do estado de imoveis e contratos.
 * Espelha as regras do dashboard (frontend/mobile {@code gerarAlertas}).
 */
public record Alerta(
        String id,
        SeveridadeAlerta severidade,
        String titulo,
        String descricao) {
}
