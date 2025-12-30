package ipn.mx.isc.sismosapp.backend.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import ipn.mx.isc.sismosapp.backend.model.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.model.entities.DeviceToken;
import ipn.mx.isc.sismosapp.backend.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final DeviceTokenRepository deviceTokenRepository;

    public NotificationService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void registrarToken(String token, String platform) {
        if (token == null || token.isBlank()) {
            return;
        }
            deviceTokenRepository.findByToken(token).ifPresentOrElse(
                    existing -> {
                        // Ya existe: actualiza plataforma si cambió (idempotente)
                        if (platform != null && !platform.isBlank() && !platform.equalsIgnoreCase(existing.getPlatform())) {
                            existing.setPlatform(platform);
                            deviceTokenRepository.save(existing);
                        }
                    },
                    () -> {
                        // No existe: intenta guardar, pero maneja la condición de carrera de duplicado
                        try {
                            String resolvedPlatform = (platform == null || platform.isBlank()) ? "android" : platform;
                            deviceTokenRepository.save(new DeviceToken(token, resolvedPlatform));
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            // Otro hilo/petición pudo insertar el mismo token en paralelo.
                            // Recupera y, si aplica, actualiza la plataforma.
                            deviceTokenRepository.findByToken(token).ifPresent(existing -> {
                                if (platform != null && !platform.isBlank() && !platform.equalsIgnoreCase(existing.getPlatform())) {
                                    existing.setPlatform(platform);
                                    deviceTokenRepository.save(existing);
                                }
                            });
                        }
                    }
            );
    }

    public void notificarSismosCriticos(List<SismoDTO> sismos, double umbralMagnitud) {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase no inicializado. Se omiten notificaciones.");
            return;
        }
        
        if (sismos == null || sismos.isEmpty()) {
            return;
        }

        List<SismoDTO> criticos = sismos.stream()
            .filter(s -> s.getMagnitud() != null && s.getMagnitud() >= umbralMagnitud)
            .toList();

        if (criticos.isEmpty()) {
            return;
        }

        List<String> tokens = deviceTokenRepository.findAll().stream()
            .map(DeviceToken::getToken)
            .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            logger.info("No hay tokens registrados; Se omiten notificaciones");
            return;
        }

        for (SismoDTO sismo : criticos) {
            enviarNotificacion(tokens, sismo);
        }
    }

    private void enviarNotificacion(List<String> tokens, SismoDTO sismo) {
        Map<String, String> data = new HashMap<>();
        data.put("id", sismo.getId());
        data.put("magnitud", String.valueOf(sismo.getMagnitud()));
        data.put("lugar", sismo.getLugar());
        data.put("profundidadKm", String.valueOf(sismo.getProfundidadKm()));
        data.put("fechaHora", sismo.getFechaHora().toString());

        Notification notification = Notification.builder()
            .setTitle("Earthquake M" + sismo.getMagnitud())
            .setBody(sismo.getLugar() + " • Depth " + sismo.getProfundidadKm() + " km")
            .build();

        for (String token : tokens) {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data)
                .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                logger.info("Notificación enviada exitosamente: {}", response);
            } catch (FirebaseMessagingException e) {
                logger.error("Error enviando notificación FCM al token {}: {}", 
                    token.substring(0, Math.min(20, token.length())) + "...", 
                    e.getMessage());
            }
        }
    }
}
