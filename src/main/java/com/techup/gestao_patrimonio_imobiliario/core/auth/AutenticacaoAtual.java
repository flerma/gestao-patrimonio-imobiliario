package com.techup.gestao_patrimonio_imobiliario.core.auth;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Dá acesso ao usuário autenticado na requisição atual, populado pelo
 * JwtAuthenticationFilter. Usado pelos services para restringir imóveis,
 * inquilinos, contratos e pagamentos ao dono da requisição.
 */
public final class AutenticacaoAtual {

    private AutenticacaoAtual() {
    }

    public static UUID usuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado");
        }
        return principal.id();
    }
}
