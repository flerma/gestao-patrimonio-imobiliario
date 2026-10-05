package com.techup.gestao_patrimonio_imobiliario.core.email;

public class EnvioEmailException extends RuntimeException {

    public EnvioEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
