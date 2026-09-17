package com.techup.gestao_patrimonio_imobiliario.core.auth;

import java.util.UUID;

/**
 * Identidade do usuário autenticado, extraída das claims do JWT
 * (JwtAuthenticationFilter) e usada como principal do SecurityContext.
 */
public record UsuarioPrincipal(UUID id, String email, String nome) {
}
