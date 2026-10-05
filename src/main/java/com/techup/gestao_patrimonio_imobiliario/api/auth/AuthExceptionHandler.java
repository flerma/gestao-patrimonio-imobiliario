package com.techup.gestao_patrimonio_imobiliario.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.techup.gestao_patrimonio_imobiliario.core.auth.CodigoInvalidoException;
import com.techup.gestao_patrimonio_imobiliario.core.auth.CredenciaisInvalidasException;
import com.techup.gestao_patrimonio_imobiliario.core.auth.EmailJaCadastradoException;
import com.techup.gestao_patrimonio_imobiliario.core.auth.LoginGoogleIndisponivelException;
import com.techup.gestao_patrimonio_imobiliario.core.auth.ReenvioCodigoMuitoRapidoException;
import com.techup.gestao_patrimonio_imobiliario.core.email.EnvioEmailException;
import com.techup.gestao_patrimonio_imobiliario.core.auth.SenhasNaoConferemException;
import com.techup.gestao_patrimonio_imobiliario.core.auth.TelefoneJaCadastradoException;

import lombok.Value;

/**
 * Tratamento dedicado às exceções do módulo de autenticação. Escopo
 * deliberadamente restrito às exceções de core.auth - nenhum outro módulo da
 * aplicação é afetado, e o comportamento padrão do Spring Boot para
 * ResponseStatusException (usado em todos os outros módulos) permanece
 * inalterado.
 */
@RestControllerAdvice(basePackages = "com.techup.gestao_patrimonio_imobiliario.api.auth")
public class AuthExceptionHandler {

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<CampoErroResponse> handleEmailJaCadastrado(EmailJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CampoErroResponse("email", ex.getMessage()));
    }

    @ExceptionHandler(TelefoneJaCadastradoException.class)
    public ResponseEntity<CampoErroResponse> handleTelefoneJaCadastrado(TelefoneJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CampoErroResponse("telefone", ex.getMessage()));
    }

    @ExceptionHandler(SenhasNaoConferemException.class)
    public ResponseEntity<ErroResponse> handleSenhasNaoConferem(SenhasNaoConferemException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(LoginGoogleIndisponivelException.class)
    public ResponseEntity<ErroResponse> handleLoginGoogleIndisponivel(LoginGoogleIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(CodigoInvalidoException.class)
    public ResponseEntity<CampoErroResponse> handleCodigoInvalido(CodigoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CampoErroResponse("codigo", ex.getMessage()));
    }

    @ExceptionHandler(ReenvioCodigoMuitoRapidoException.class)
    public ResponseEntity<ErroResponse> handleReenvioMuitoRapido(ReenvioCodigoMuitoRapidoException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(EnvioEmailException.class)
    public ResponseEntity<ErroResponse> handleEnvioEmail(EnvioEmailException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroResponse(ex.getMessage()));
    }

    @Value
    public static class ErroResponse {
        String message;
    }

    @Value
    public static class CampoErroResponse {
        String campo;
        String message;
    }
}
