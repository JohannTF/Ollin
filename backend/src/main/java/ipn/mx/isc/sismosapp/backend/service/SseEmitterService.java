package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {

    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long SSE_TIMEOUT = 60 * 60 * 1000L; // 1 hora

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter crearEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.info("SSE emitter completado. Activos: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            logger.info("SSE emitter timeout. Activos: {}", emitters.size());
        });

        emitter.onError((ex) -> {
            emitters.remove(emitter);
            logger.error("SSE emitter error. Activos: {}", emitters.size());
        });

        emitters.add(emitter);
        logger.info("Nuevo SSE emitter creado. Total activos: {}", emitters.size());

        return emitter;
    }

    public void enviarNuevosSismos(List<SismoDTO> nuevosSismos) {
        if (emitters.isEmpty()) {
            logger.debug("No hay clientes SSE conectados");
            return;
        }

        logger.info("Enviando {} nuevos sismos a {} clientes SSE", nuevosSismos.size(), emitters.size());

        List<SseEmitter> emittersInactivos = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("nuevos-sismos")
                    .data(nuevosSismos));
            } catch (IOException e) {
                logger.error("Error enviando evento SSE: {}", e.getMessage());
                emittersInactivos.add(emitter);
            }
        }

        emitters.removeAll(emittersInactivos);
        logger.info("Emitters inactivos removidos: {}. Activos: {}", emittersInactivos.size(), emitters.size());
    }
}
