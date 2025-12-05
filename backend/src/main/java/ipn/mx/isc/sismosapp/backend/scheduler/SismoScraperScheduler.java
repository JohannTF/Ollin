package ipn.mx.isc.sismosapp.backend.scheduler;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.mapper.SismoMapper;
import ipn.mx.isc.sismosapp.backend.model.Sismo;
import ipn.mx.isc.sismosapp.backend.repository.SismoRepository;
import ipn.mx.isc.sismosapp.backend.service.RedisCacheService;
import ipn.mx.isc.sismosapp.backend.service.ScraperService;
import ipn.mx.isc.sismosapp.backend.service.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler responsable del scraping periódico. 
 * Actualizar la BD y cache
 */
@Component
public class SismoScraperScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SismoScraperScheduler.class);
    private static final int MAX_SISMOS_RECIENTES = 100;

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private SismoRepository sismoRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private SismoMapper sismoMapper;

    /**
     * Ejecuta el scraping cada 60 segundos
     * Extrae sismos del SSN, los almacena en BD y actualiza cache
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scrapearYAlmacenarSismos() {

        List<Sismo> sismosExtraidos = scraperService.scrapeSismos();

        if (sismosExtraidos.isEmpty()) {
            logger.warn("No se extrajeron sismos del scrapper");
            return;
        }

        // Obtener snapshot actual de Redis para verificación rápida
        List<SismoDTO> sismosEnCache = redisCacheService.obtenerSismosRecientes();
        
        List<SismoDTO> nuevosSismos = new ArrayList<>();
        int nuevos = 0;
        int duplicados = 0;

        for (Sismo sismo : sismosExtraidos) {
            // Verificar si ya existe en cache (optimización)
            if (sismosEnCache != null && existeEnCache(sismo, sismosEnCache)) {
                duplicados++;
                continue;
            }

            // Verificar en BD (solo si no está en cache)
            if (sismoRepository.findByFechaHoraAndLatitudAndLongitudAndMagnitud(
                sismo.getFechaHora(),
                sismo.getLatitud(),
                sismo.getLongitud(),
                sismo.getMagnitud()
            ).isEmpty()) {
                Sismo sismoGuardado = sismoRepository.save(sismo);
                SismoDTO nuevoSismoDTO = sismoMapper.toDTO(sismoGuardado);
                nuevosSismos.add(nuevoSismoDTO);
                nuevos++;
            } else {
                duplicados++;
            }
        }

        logger.info("Scraping finalizado. Nuevos: {}, Duplicados: {}", nuevos, duplicados);

        // Si hay nuevos sismos, actualizar cache y notificar via SSE
        if (!nuevosSismos.isEmpty()) {
            actualizarCacheConNuevosSismos();
            sseEmitterService.enviarNuevosSismos(nuevosSismos);
            logger.info("Cache actualizado y {} nuevos sismos enviados via SSE", nuevosSismos.size());
        }
        
        logger.info(null);
    }

    /**
     * Verifica si un sismo ya existe en el cache
     * Usa comparación por campos clave con tolerancia para coordenadas
     */
    private boolean existeEnCache(Sismo sismo, List<SismoDTO> cache) {
        return cache.stream().anyMatch(dto ->
            dto.getFechaHora().equals(sismo.getFechaHora()) &&
            Math.abs(dto.getLatitud() - sismo.getLatitud()) < 0.0001 &&
            Math.abs(dto.getLongitud() - sismo.getLongitud()) < 0.0001 &&
            Math.abs(dto.getMagnitud() - sismo.getMagnitud()) < 0.01
        );
    }

    /**
     * Actualiza el cache de Redis con los sismos más recientes de la BD
     */
    private void actualizarCacheConNuevosSismos() {
        var pageable = PageRequest.of(0, MAX_SISMOS_RECIENTES, Sort.by(Sort.Direction.DESC, "fechaHora"));
        List<SismoDTO> sismosActualizados = sismoMapper.toDTOList(
            sismoRepository.findAll(pageable).getContent()
        );
        redisCacheService.guardarSismosRecientes(sismosActualizados);
    }
}
