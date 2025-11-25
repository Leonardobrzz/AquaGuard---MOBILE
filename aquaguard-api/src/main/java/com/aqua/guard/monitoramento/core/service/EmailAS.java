package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.core.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailAS {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("hello@demomailtrap.co");

        message.setTo(to);
        message.setSubject("AquaGuard - Código de Verificação");
        message.setText("Olá! Seu código de verificação é: " + code);

        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hello@demomailtrap.co");
        message.setTo(to);
        message.setSubject("AquaGuard - Reset de Senha");

        String corpoEmail = String.format(
                "Olá!\n\n" +
                        "Você solicitou o reset da sua senha no AquaGuard.\n" +
                        "Seu código de verificação é: %s\n\n" +
                        "Este código expira em 15 minutos.\n" +
                        "Se você não solicitou esta alteração, ignore este email.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe AquaGuard",
                code
        );
        message.setText(corpoEmail);

        mailSender.send(message);
    }

    @Async
    public void enviarAlertaNivelBaixo(Usuario usuario, String nomeCaixa, BigDecimal percentual) {
        if (!usuario.isNotificacoesEmailAtivas()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hello@demomailtrap.co");
        message.setTo(usuario.getEmail());
        message.setSubject("AquaGuard - Alerta de Nível Baixo!");

        String corpoEmail = String.format(
                "Olá, %s!\n\n" +
                        "Este é um alerta para informar que o nível da sua caixa d'água '%s' está baixo.\n" +
                        "Nível atual: %.0f%%\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe AquaGuard",
                usuario.getNomeCompleto(),
                nomeCaixa,
                percentual
        );
        message.setText(corpoEmail);

        mailSender.send(message);
    }
}
