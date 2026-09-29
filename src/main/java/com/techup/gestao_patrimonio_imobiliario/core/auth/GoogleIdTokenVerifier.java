package com.techup.gestao_patrimonio_imobiliario.core.auth;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Valida ID tokens do Google (Sign in with Google): assinatura pelas chaves
 * publicas do Google (JWKS, com cache do proprio Nimbus), emissor, validade,
 * audiencia (um dos client IDs OAuth configurados) e e-mail verificado.
 *
 * <p>Os client IDs vem de {@code security.google.client-ids} (env
 * {@code GOOGLE_CLIENT_IDS}), separados por virgula - normalmente so o client
 * "Aplicativo da Web", que tambem e o usado pelo app mobile para pedir o ID
 * token (webClientId).
 */
@Component
public class GoogleIdTokenVerifier {

    private static final String JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> EMISSORES = Set.of("accounts.google.com", "https://accounts.google.com");

    private final List<String> clientIds;
    private volatile JwtDecoder decoder;

    public GoogleIdTokenVerifier(@Value("${security.google.client-ids:}") String clientIds) {
        this.clientIds = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
    }

    public GoogleIdentidade verificar(String idToken) {
        if (clientIds.isEmpty()) {
            throw new LoginGoogleIndisponivelException("Login com Google não está configurado no servidor.");
        }
        Jwt jwt;
        try {
            jwt = decoder().decode(idToken);
        } catch (JwtException e) {
            throw new CredenciaisInvalidasException("Token do Google inválido ou expirado");
        }
        if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
            throw new CredenciaisInvalidasException("O e-mail da conta Google não está verificado");
        }
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new CredenciaisInvalidasException("A conta Google não informou um e-mail");
        }
        String nome = jwt.getClaimAsString("name");
        return new GoogleIdentidade(jwt.getSubject(), email, nome != null && !nome.isBlank() ? nome : email);
    }

    private JwtDecoder decoder() {
        if (decoder == null) {
            synchronized (this) {
                if (decoder == null) {
                    NimbusJwtDecoder nimbus = NimbusJwtDecoder.withJwkSetUri(JWKS_URI).build();
                    nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                            new JwtTimestampValidator(),
                            validarClaim("iss", jwt -> jwt.getIssuer() != null
                                    && EMISSORES.contains(jwt.getIssuer().toString())),
                            validarClaim("aud", jwt -> jwt.getAudience() != null
                                    && jwt.getAudience().stream().anyMatch(clientIds::contains))));
                    decoder = nimbus;
                }
            }
        }
        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> validarClaim(String claim, Predicate<Jwt> regra) {
        return jwt -> regra.test(jwt)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Claim invalida: " + claim, null));
    }
}
