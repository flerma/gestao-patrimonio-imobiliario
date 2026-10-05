package com.techup.gestao_patrimonio_imobiliario.core.email;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Envio de e-mails transacionais via SMTP (spring.mail.*: MAIL_HOST,
 * MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD; remetente em app.mail.remetente /
 * MAIL_FROM). Funciona com qualquer provedor SMTP (Gmail com senha de app,
 * Amazon SES, Brevo, Resend etc.).
 *
 * <p>Sem MAIL_HOST configurado (ambiente local), o e-mail nao e enviado: o
 * conteudo vai para o log, para permitir testar o fluxo sem um servidor SMTP.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String host;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${spring.mail.host:}") String host,
                        @Value("${app.mail.remetente:}") String remetente) {
        this.mailSender = mailSender;
        this.host = host;
        this.remetente = remetente;
    }

    /** @throws EnvioEmailException se o servidor SMTP recusar ou estiver inacessivel. */
    public void enviarHtml(String para, String assunto, String html) {
        JavaMailSender sender = host.isBlank() ? null : mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP nao configurado (MAIL_HOST vazio) - e-mail NAO enviado. Para: {} | Assunto: {}\n{}",
                    para, assunto, html);
            return;
        }
        try {
            MimeMessage mensagem = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, false, StandardCharsets.UTF_8.name());
            helper.setTo(para);
            if (!remetente.isBlank()) {
                helper.setFrom(remetente);
            }
            helper.setSubject(assunto);
            helper.setText(html, true);
            sender.send(mensagem);
        } catch (MessagingException | MailException e) {
            log.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
            throw new EnvioEmailException("Não foi possível enviar o e-mail. Tente novamente em instantes.", e);
        }
    }
}
