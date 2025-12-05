package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.mapper.SismoMapper;
import ipn.mx.isc.sismosapp.backend.repository.SismoRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SismoService {

    private static final Logger logger = LoggerFactory.getLogger(SismoService.class);
    private static final int MAX_SISMOS_RECIENTES = 100;

    @Autowired
    private SismoRepository sismoRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private SismoMapper sismoMapper;

    /**
     * Inicializa el cache Redis al arrancar la aplicación
     */
    @PostConstruct
    public void inicializarCache() {
        logger.info("Inicializando cache Redis con los sismos más recientes de BD...");
        
        List<SismoDTO> sismosRecientes = obtenerSismosDesdeDB(0, MAX_SISMOS_RECIENTES);
    
        if (!sismosRecientes.isEmpty()) {
            redisCacheService.guardarSismosRecientes(sismosRecientes);
            logger.info("Cache Redis actualizado con {} sismos desde BD", sismosRecientes.size());
        } else {
            logger.info("No hay sismos en BD para inicializar cache");
        }
    }

    /**
     * Obtiene sismos con paginación
     * Usa cache Redis para la primera página cuando es posible
     */
    public List<SismoDTO> obtenerTodosLosSismos(int page, int size) {
        // Primero intenta Redis para los recientes
        if (page == 0 && size <= MAX_SISMOS_RECIENTES) {
            List<SismoDTO> cached = redisCacheService.obtenerSismosRecientes();
            if (cached != null && !cached.isEmpty()) {
                return cached.stream().limit(size).collect(Collectors.toList());
            }
        }

        // Si no está en cache, consulta BD
        List<SismoDTO> sismos = obtenerSismosDesdeDB(page, size);
        
        return sismos;
    }

    /**
     * Obtiene sismos desde la base de datos con paginación
     * @param page Número de página
     * @param size Cantidad de elementos por página
     * @return Lista de sismos ordenados por fecha descendente
     */
    private List<SismoDTO> obtenerSismosDesdeDB(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"));
        return sismoMapper.toDTOList(
            sismoRepository.findAll(pageable).getContent()
        );
    }
}
