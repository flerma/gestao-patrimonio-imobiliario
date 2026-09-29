package com.techup.gestao_patrimonio_imobiliario.core.auth;

/** Login com Google chamado sem nenhum client ID configurado (security.google.client-ids). */
public class LoginGoogleIndisponivelException extends RuntimeException {

    public LoginGoogleIndisponivelException(String message) {
        super(message);
    }
}
