package com.techup.gestao_patrimonio_imobiliario.core.auth;

/** Novo codigo pedido antes do intervalo minimo entre envios. */
public class ReenvioCodigoMuitoRapidoException extends RuntimeException {

    public ReenvioCodigoMuitoRapidoException(String message) {
        super(message);
    }
}
