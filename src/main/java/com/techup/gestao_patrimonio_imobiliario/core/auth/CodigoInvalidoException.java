package com.techup.gestao_patrimonio_imobiliario.core.auth;

/** Codigo de redefinicao de senha errado, expirado ou ja usado. */
public class CodigoInvalidoException extends RuntimeException {

    public CodigoInvalidoException() {
        this("Código inválido.");
    }

    public CodigoInvalidoException(String message) {
        super(message);
    }
}
