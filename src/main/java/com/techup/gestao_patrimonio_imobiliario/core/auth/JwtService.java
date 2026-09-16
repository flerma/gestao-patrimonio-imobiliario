package com.techup.gestao_patrimonio_imobiliario.core.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Geração e validação do access token (JWT) usado pela API.
 * O refresh token NÃO é gerado aqui - é um valor opaco tratado pelo AuthService.
 */
@Service
public class JwtService {

    private final SecretKey chaveAssinatura;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        this.chaveAssinatura = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String gerarAccessToken(Usuario usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chaveAssinatura)
                .compact();
    }

    public Claims validarEExtrairClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(chaveAssinatura)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new CredenciaisInvalidasException("Token inválido ou expirado");
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
}
