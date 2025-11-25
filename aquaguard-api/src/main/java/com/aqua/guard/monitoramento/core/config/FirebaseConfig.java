package com.aqua.guard.monitoramento.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${aquaguard.firebase.service-account-file-path}")
    private String serviceAccountFilePath;

    @PostConstruct
    public void initializeFirebase() {
        try {
            InputStream serviceAccount = new ClassPathResource(serviceAccountFilePath).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK inicializado com sucesso.");
            }
        } catch (IOException e) {
            logger.error("Erro ao inicializar o Firebase Admin SDK", e);
        }
    }
}