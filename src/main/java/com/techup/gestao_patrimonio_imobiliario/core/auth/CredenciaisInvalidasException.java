package com.techup.gestao_patrimonio_imobiliario.core.auth;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
