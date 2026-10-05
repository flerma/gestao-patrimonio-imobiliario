package com.techup.gestao_patrimonio_imobiliario.core.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import com.techup.gestao_patrimonio_imobiliario.core.email.EmailService;
import com.techup.gestao_patrimonio_imobiliario.data.auth.CodigoRedefinicaoSenhaEntity;
import com.techup.gestao_patrimonio_imobiliario.data.auth.CodigoRedefinicaoSenhaRepository;
import com.techup.gestao_patrimonio_imobiliario.data.auth.RefreshTokenRepository;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;

/**
 * "Esqueceu a sua senha?": envia por e-mail um codigo aleatorio de 6 digitos
 * (valido por 30 minutos) e, com ele, permite definir uma nova senha.
 *
 * <ul>
 *   <li>Guarda so o hash do codigo; cada novo envio invalida os anteriores -
 *       vale sempre o ultimo codigo enviado.</li>
 *   <li>E-mail nao cadastrado: responde igual (sem enviar nada), para nao
 *       revelar quais e-mails existem.</li>
 *   <li>Protecoes: intervalo minimo entre envios e limite de tentativas erradas
 *       por codigo (depois disso o codigo e descartado e e preciso reenviar).</li>
 *   <li>Ao trocar a senha, todas as sessoes (refresh tokens) do usuario sao
 *       revogadas.</li>
 * </ul>
 */
@Service
@Transactional
public class RedefinicaoSenhaService {

    static final Duration VALIDADE_CODIGO = Duration.ofMinutes(30);
    static final Duration INTERVALO_MINIMO_ENTRE_ENVIOS = Duration.ofSeconds(60);
    static final int MAX_TENTATIVAS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CodigoRedefinicaoSenhaRepository codigoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RedefinicaoSenhaService(UsuarioRepository usuarioRepository,
                                   CodigoRedefinicaoSenhaRepository codigoRepository,
                                   RefreshTokenRepository refreshTokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.codigoRepository = codigoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /** Gera, guarda e envia um novo codigo (usado tanto no primeiro envio quanto no "Reenviar codigo"). */
    public void solicitarCodigo(String email) {
        Optional<UsuarioEntity> encontrado = usuarioRepository.findByEmailIgnoreCase(email.trim());
        if (encontrado.isEmpty()) {
            return;
        }
        UsuarioEntity usuario = encontrado.get();
        LocalDateTime agora = LocalDateTime.now();

        codigoRepository.findFirstByUsuarioIdOrderByDataCriacaoDesc(usuario.getId()).ifPresent(ultimo -> {
            long segundos = Duration.between(ultimo.getDataCriacao(), agora).getSeconds();
            long faltam = INTERVALO_MINIMO_ENTRE_ENVIOS.getSeconds() - segundos;
            if (faltam > 0) {
                throw new ReenvioCodigoMuitoRapidoException(
                        "Aguarde " + faltam + " segundos para solicitar um novo código.");
            }
        });

        // Vale sempre o ultimo codigo enviado: invalida os anteriores.
        codigoRepository.findByUsuarioIdAndUsadoFalse(usuario.getId()).forEach(c -> c.setUsado(true));

        String codigo = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        codigoRepository.save(CodigoRedefinicaoSenhaEntity.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuario.getId())
                .codigoHash(hash(usuario.getId(), codigo))
                .dataExpiracao(agora.plus(VALIDADE_CODIGO))
                .usado(false)
                .tentativas(0)
                .dataCriacao(agora)
                .build());

        // Dentro da transacao: se o e-mail falhar, o codigo novo nao fica gravado.
        emailService.enviarHtml(usuario.getEmail(), "Seu código para redefinir a senha",
                montarEmail(usuario.getNome(), codigo));
    }

    /**
     * Confere o codigo e grava a nova senha. Codigo errado, expirado, ja usado
     * ou de e-mail inexistente: sempre {@link CodigoInvalidoException}.
     */
    @Transactional(noRollbackFor = CodigoInvalidoException.class)
    public void redefinirSenha(String email, String codigo, String novaSenha, String confirmarSenha) {
        if (!novaSenha.equals(confirmarSenha)) {
            throw new SenhasNaoConferemException("As senhas informadas não conferem");
        }
        UsuarioEntity usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(CodigoInvalidoException::new);
        CodigoRedefinicaoSenhaEntity atual = codigoRepository
                .findFirstByUsuarioIdAndUsadoFalseOrderByDataCriacaoDesc(usuario.getId())
                .orElseThrow(CodigoInvalidoException::new);

        if (atual.getDataExpiracao().isBefore(LocalDateTime.now())) {
            atual.setUsado(true);
            throw new CodigoInvalidoException("Código expirado. Clique em \"Reenviar código\" para receber um novo.");
        }
        boolean confere = MessageDigest.isEqual(
                atual.getCodigoHash().getBytes(StandardCharsets.UTF_8),
                hash(usuario.getId(), codigo.trim()).getBytes(StandardCharsets.UTF_8));
        if (!confere) {
            atual.setTentativas(atual.getTentativas() + 1);
            if (atual.getTentativas() >= MAX_TENTATIVAS) {
                atual.setUsado(true);
                throw new CodigoInvalidoException(
                        "Código inválido. Limite de tentativas atingido: clique em \"Reenviar código\".");
            }
            throw new CodigoInvalidoException();
        }

        atual.setUsado(true);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);
        // Encerra as sessoes abertas com a senha antiga.
        refreshTokenRepository.findByUsuarioIdAndRevogadoFalse(usuario.getId())
                .forEach(t -> t.setRevogado(true));
    }

    private static String hash(UUID usuarioId, String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((usuarioId + ":" + codigo).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível", e);
        }
    }

    private static String montarEmail(String nome, String codigo) {
        String nomeSeguro = HtmlUtils.htmlEscape(nome == null ? "" : nome);
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:0 auto;color:#0C1A33">
                  <h2 style="color:#003C85;margin-bottom:8px">Redefinição de senha</h2>
                  <p>Olá, %s.</p>
                  <p>Use o código abaixo para criar uma nova senha no Gestão Seu Patrimônio:</p>
                  <p style="font-size:32px;font-weight:bold;letter-spacing:8px;background:#F5F7FA;
                            border:1px solid #DFE5EE;border-radius:8px;padding:16px;text-align:center;color:#003C85">%s</p>
                  <p>O código é válido por <strong>30 minutos</strong>. Se você pedir um novo código,
                     este deixa de valer.</p>
                  <p style="color:#62708A;font-size:13px">Se você não pediu a redefinição de senha, ignore este
                     e-mail — sua senha atual continua a mesma.</p>
                </div>
                """.formatted(nomeSeguro, codigo);
    }
}
