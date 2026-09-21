package com.techup.gestao_patrimonio_imobiliario.core.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techup.gestao_patrimonio_imobiliario.core.enums.ProvedorAutenticacao;
import com.techup.gestao_patrimonio_imobiliario.core.enums.RoleUsuario;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusUsuario;
import com.techup.gestao_patrimonio_imobiliario.core.usuario.Usuario;
import com.techup.gestao_patrimonio_imobiliario.data.auth.RefreshTokenEntity;
import com.techup.gestao_patrimonio_imobiliario.data.auth.RefreshTokenRepository;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioMapper;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;

@Service
@Transactional
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public TokenPair login(String email, String senha) {
        UsuarioEntity entity = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CredenciaisInvalidasException("Usuário ou senha inválidos"));

        if (entity.getSenha() == null || !passwordEncoder.matches(senha, entity.getSenha())) {
            throw new CredenciaisInvalidasException("Usuário ou senha inválidos");
        }

        Usuario usuario = UsuarioMapper.toDomain(entity);
        String accessToken = jwtService.gerarAccessToken(usuario);
        String refreshTokenPlaintext = gerarRefreshTokenOpaco();
        persistirRefreshToken(usuario.getId(), refreshTokenPlaintext);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenPlaintext)
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .usuario(usuario)
                .build();
    }

    public Usuario registrar(String nome, String email, String telefone, String senha, String confirmarSenha) {
        if (!senha.equals(confirmarSenha)) {
            throw new SenhasNaoConferemException("As senhas informadas não conferem");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Já existe um usuário cadastrado com este e-mail");
        }
        if (usuarioRepository.existsByTelefone(telefone)) {
            throw new TelefoneJaCadastradoException("Já existe um usuário cadastrado com este telefone");
        }

        LocalDateTime agora = LocalDateTime.now();
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .senha(passwordEncoder.encode(senha))
                .provedorAutenticacao(ProvedorAutenticacao.LOCAL)
                .status(StatusUsuario.ATIVO)
                // Autocadastro nunca cria ADMIN - so um ADMIN pode promover alguem
                // a ADMIN, via tela de gestao de usuarios (UsuarioController).
                .role(RoleUsuario.USUARIO)
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();

        UsuarioEntity salvo = usuarioRepository.save(UsuarioMapper.toEntity(usuario));
        return UsuarioMapper.toDomain(salvo);
    }

    public String refresh(String refreshTokenPlaintext) {
        String hash = hashSha256(refreshTokenPlaintext);
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new CredenciaisInvalidasException("Refresh token inválido ou expirado"));

        if (refreshTokenEntity.isRevogado() || refreshTokenEntity.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new CredenciaisInvalidasException("Refresh token inválido ou expirado");
        }

        UsuarioEntity usuarioEntity = usuarioRepository.findById(refreshTokenEntity.getUsuarioId())
                .orElseThrow(() -> new CredenciaisInvalidasException("Refresh token inválido ou expirado"));

        Usuario usuario = UsuarioMapper.toDomain(usuarioEntity);
        return jwtService.gerarAccessToken(usuario);
    }

    public void logout(String refreshTokenPlaintext) {
        String hash = hashSha256(refreshTokenPlaintext);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(refreshTokenEntity -> {
            refreshTokenEntity.setRevogado(true);
            refreshTokenRepository.save(refreshTokenEntity);
        });
    }

    private void persistirRefreshToken(UUID usuarioId, String refreshTokenPlaintext) {
        LocalDateTime agora = LocalDateTime.now();
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .tokenHash(hashSha256(refreshTokenPlaintext))
                .dataExpiracao(agora.plus(Duration.ofMillis(refreshTokenExpirationMs)))
                .revogado(false)
                .dataCriacao(agora)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private static String gerarRefreshTokenOpaco() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashSha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível", e);
        }
    }
}
