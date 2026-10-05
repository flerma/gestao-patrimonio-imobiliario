package com.techup.gestao_patrimonio_imobiliario.core.auth;

/** "Esqueceu a sua senha?" com um e-mail que nao pertence a nenhum usuario. */
public class EmailNaoCadastradoException extends RuntimeException {

    public EmailNaoCadastradoException(String message) {
        super(message);
    }
}
