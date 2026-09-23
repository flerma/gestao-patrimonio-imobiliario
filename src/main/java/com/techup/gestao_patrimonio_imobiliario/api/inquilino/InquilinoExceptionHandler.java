package com.techup.gestao_patrimonio_imobiliario.api.inquilino;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.techup.gestao_patrimonio_imobiliario.core.inquilino.DocumentoJaCadastradoException;

import lombok.Value;

/**
 * Tratamento dedicado às exceções do módulo de inquilinos. Escopo
 * deliberadamente restrito às exceções de core.inquilino, espelhando
 * {@code AuthExceptionHandler} - nenhum outro módulo é afetado.
 */
@RestControllerAdvice(basePackages = "com.techup.gestao_patrimonio_imobiliario.api.inquilino")
public class InquilinoExceptionHandler {

    @ExceptionHandler(DocumentoJaCadastradoException.class)
    public ResponseEntity<CampoErroResponse> handleDocumentoJaCadastrado(DocumentoJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CampoErroResponse("documento", ex.getMessage()));
    }

    @Value
    public static class CampoErroResponse {
        String campo;
        String message;
    }
}
