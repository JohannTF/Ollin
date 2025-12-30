package ipn.mx.isc.sismosapp.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import ipn.mx.isc.sismosapp.backend.model.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.model.entities.DeviceToken;
import ipn.mx.isc.sismosapp.backend.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para enviar mensajes FCM de tipo data (sin notificación visible)
*/
@Service
public class FcmDataMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(FcmDataMessagingService.class);
    private static final String SISMOS_DATA_KEY = "sismos";

    private final DeviceTokenRepository deviceTokenRepository;
    private final ObjectMapper objectMapper;

    public FcmDataMessagingService(DeviceTokenRepository deviceTokenRepository,
                                   ObjectMapper objectMapper) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Envía una lista de sismos a todos los dispositivos registrados mediante mensaje data FCM.
     * El payload contiene los sismos serializados como JSON bajo la clave "sismos".
     *
     * @param sismos Lista de sismos a enviar
     */
    public void enviarSismosADispositivos(List<SismoDTO> sismos) {
        if (sismos == null || sismos.isEmpty()) {
            logger.debug("No hay sismos para enviar");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase no inicializado. Se omiten mensajes data FCM.");
            return;
        }

        // Obtener todos los tokens registrados
        List<String> tokens = deviceTokenRepository.findAll().stream()
            .map(DeviceToken::getToken)
            .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            logger.info("No hay tokens registrados; se omiten mensajes data FCM");
            return;
        }

        // Serializar sismos a JSON
        String sismosJson;
        try {
            sismosJson = objectMapper.writeValueAsString(sismos);
        } catch (JsonProcessingException e) {
            logger.error("No se pudo serializar la lista de sismos a JSON", e);
            return;
        }

        // Crear map de datos
        Map<String, String> data = new HashMap<>();
        data.put(SISMOS_DATA_KEY, sismosJson);

        // Enviar a cada token
        List<String> tokensInvalidos = new ArrayList<>();

        for (String token : tokens) {
            try {
                Message message = Message.builder()
                    .putAllData(data)
                    .setToken(token)
                    .build();

                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                // Marcar token como inválido para limpieza posterior
                if (e.getErrorCode() != null && (
                    e.getErrorCode().equals("invalid-argument") ||
                    e.getErrorCode().equals("registration-token-not-registered"))) {
                    tokensInvalidos.add(token);
                }
            }
        }

        // Limpiar tokens inválidos de la BD
        if (!tokensInvalidos.isEmpty()) {
            for (String token : tokensInvalidos) {
            deviceTokenRepository.findByToken(token).ifPresent(
                deviceToken -> deviceTokenRepository.delete(deviceToken)
            );
        } 
        }
    }
}
