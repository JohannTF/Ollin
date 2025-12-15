package ipn.mx.isc.sismosapp.backend.configurations;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${FIREBASE_SERVICE_ACCOUNT_PATH:}")
    private String firebaseServiceAccountPath;

    @PostConstruct
    public void initializeFirebase() {
        if (firebaseServiceAccountPath == null || firebaseServiceAccountPath.isBlank()) {
            logger.warn("FIREBASE_SERVICE_ACCOUNT_PATH no configurado. Las notificaciones push no funcionarán.");
            return;
        }

        try {
            FileInputStream serviceAccount = new FileInputStream(firebaseServiceAccountPath);
            
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK Inicializado Correctamente");
            } else {
                logger.debug("Firebase Admin SDK ya estaba inicializado");
            }
        } catch (IOException e) {
            logger.error("ERROR: No se pudo inicializar Firebase Admin SDK", e);
        }
    }
}
