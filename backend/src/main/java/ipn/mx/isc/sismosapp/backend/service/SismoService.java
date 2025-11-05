package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.model.Sismo;
import ipn.mx.isc.sismosapp.backend.repository.SismoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SismoService {

    private static final Logger logger = LoggerFactory.getLogger(SismoService.class);

    @Autowired
    private SismoRepository sismoRepository;

    @Autowired
    private ScraperService scraperService;

    private Set<String> cacheKeys = new HashSet<>();

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void scrapearYAlmacenarSismos() {
        logger.info("Ejecutando scraping");

        List<Sismo> sismosExtraidos = scraperService.scrapeSismos();

        if (sismosExtraidos.isEmpty()) {
            logger.warn("No se extrajeron sismos en esta ejecucion");
            return;
        }

        int nuevos = 0;
        int duplicados = 0;

        for (Sismo sismo : sismosExtraidos) {
            String cacheKey = generarCacheKey(sismo);

            if (cacheKeys.contains(cacheKey)) {
                duplicados++;
                continue;
            }

            if (sismoRepository.findByFechaHoraAndLatitudAndLongitudAndMagnitud(
                sismo.getFechaHora(),
                sismo.getLatitud(),
                sismo.getLongitud(),
                sismo.getMagnitud()
            ).isEmpty()) {
                sismoRepository.save(sismo);
                cacheKeys.add(cacheKey);
                nuevos++;
            } else {
                duplicados++;
                cacheKeys.add(cacheKey);
            }
        }

        logger.info("Scraping finalizado. Nuevos: {}, Duplicados: {}", nuevos, duplicados);
    }

    private String generarCacheKey(Sismo sismo) {
        return String.format("%s|%.6f|%.6f|%.1f",
            sismo.getFechaHora().toString(),
            sismo.getLatitud(),
            sismo.getLongitud(),
            sismo.getMagnitud()
        );
    }

    public List<SismoDTO> obtenerTodosLosSismos(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"));
        return sismoRepository.findAll(pageable)
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    public List<SismoDTO> obtenerSismosRecientes(int horas) {
        OffsetDateTime fechaInicio = OffsetDateTime.now(ZoneOffset.UTC).minusHours(horas);
        return sismoRepository.findRecentSismos(fechaInicio)
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    public List<SismoDTO> obtenerSismosPorMagnitud(Double magnitudMinima) {
        return sismoRepository.findByMagnitudMinima(magnitudMinima)
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    private SismoDTO convertirADTO(Sismo sismo) {
        return new SismoDTO(
            sismo.getId(),
            sismo.getFechaHora(),
            sismo.getLatitud(),
            sismo.getLongitud(),
            sismo.getMagnitud(),
            sismo.getProfundidadKm(),
            sismo.getLugar(),
            sismo.getFuente()
        );
    }
}
