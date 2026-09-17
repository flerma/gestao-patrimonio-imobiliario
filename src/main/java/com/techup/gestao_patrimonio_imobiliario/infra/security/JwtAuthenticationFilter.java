package com.techup.gestao_patrimonio_imobiliario.infra.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.techup.gestao_patrimonio_imobiliario.core.auth.JwtService;
import com.techup.gestao_patrimonio_imobiliario.core.auth.UsuarioPrincipal;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lê o header Authorization: Bearer <token>, valida o JWT e, se válido,
 * popula o SecurityContext. Não lança exceção para token ausente/inválido -
 * apenas deixa a requisição seguir sem autenticação, e o
 * authorizeHttpRequests do SecurityConfig se encarrega de barrar o acesso a
 * rotas protegidas.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtService.validarEExtrairClaims(token);
                UsuarioPrincipal principal = new UsuarioPrincipal(
                        UUID.fromString(claims.getSubject()),
                        claims.get("email", String.class),
                        claims.get("nome", String.class));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException e) {
                // Token ausente/inválido/expirado: segue sem autenticar, deixa o
                // authorizeHttpRequests decidir se a rota exige autenticação.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
