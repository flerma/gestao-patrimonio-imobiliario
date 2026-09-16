package com.techup.gestao_patrimonio_imobiliario.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techup.gestao_patrimonio_imobiliario.api.usuario.UsuarioResponse;
import com.techup.gestao_patrimonio_imobiliario.core.auth.AuthService;
import com.techup.gestao_patrimonio_imobiliario.core.auth.JwtService;
import com.techup.gestao_patrimonio_imobiliario.core.auth.TokenPair;
import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login, cadastro e renovação de sessão via JWT")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um usuário com e-mail e senha e retorna o par de tokens (access + refresh).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
    })
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokenPair = authService.login(request.getUsuario(), request.getSenha());
        return ResponseEntity.ok(toTokenResponse(tokenPair));
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar", description = "Cria uma nova conta de usuário local (e-mail/senha).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senhas não conferem", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail ou telefone já cadastrado", content = @Content)
    })
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody CadastroRequest request) {
        Usuario usuario = authService.registrar(
                request.getNome(),
                request.getEmail(),
                request.getTelefone(),
                request.getSenha(),
                request.getConfirmarSenha());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(usuario));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Emite um novo access token a partir de um refresh token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo access token emitido"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou revogado", content = @Content)
    })
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        String accessToken = authService.refresh(request.getRefreshToken());
        TokenResponse response = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .usuario(null)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga o refresh token informado, encerrando a sessão.")
    @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    private TokenResponse toTokenResponse(TokenPair tokenPair) {
        return TokenResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getExpiresIn())
                .usuario(UsuarioResponse.from(tokenPair.getUsuario()))
                .build();
    }
}
