package com.aqua.guard.monitoramento.core.service;

import com.aqua.guard.monitoramento.core.entity.DispositivoUsuario;
import com.aqua.guard.monitoramento.core.entity.Usuario;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PushNotificationAS {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationAS.class);

    @Autowired
    private DispositivoUsuarioAS dispositivoUsuarioAS;

    public void enviarNotificacao(Usuario usuario, String titulo, String corpo) {
        if (!usuario.isNotificacoesPushAtivas()) {
            return;
        }

        List<String> tokens = usuario.getDispositivos().stream()
                .map(DispositivoUsuario::getPushToken)
                .toList();

        if (tokens.isEmpty()) {
            return;
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(titulo)
                        .setBody(corpo)
                        .build())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            if (response.getFailureCount() > 0) {
                List<String> tokensInvalidos = new ArrayList<>();
                List<SendResponse> responses = response.getResponses();

                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        String tokenFalho = tokens.get(i);
                        tokensInvalidos.add(tokenFalho);
                        logger.warn("Falha ao enviar notificação para o token: {}. Motivo: {}",
                                tokenFalho, responses.get(i).getException().getMessage());
                    }
                }

                limparTokensInvalidos(tokensInvalidos);
            }

            logger.info("Notificações enviadas com sucesso: {}. Falhas: {}",
                    response.getSuccessCount(), response.getFailureCount());

        } catch (FirebaseMessagingException e) {
            logger.error("Erro ao enviar lote de notificações para o Firebase", e);
        }
    }

    private void limparTokensInvalidos(List<String> tokens) {
        if (tokens.isEmpty()) {
            return;
        }

        for (String token : tokens) {
            dispositivoUsuarioAS.excluirPorToken(token);
            logger.info("Solicitada a remoção do token inválido: {}", token);
        }
    }

}
