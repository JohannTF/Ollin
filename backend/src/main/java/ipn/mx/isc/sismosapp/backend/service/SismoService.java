package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.dto.SismoDTO;
import ipn.mx.isc.sismosapp.backend.dto.SismoFilterDTO;
import ipn.mx.isc.sismosapp.backend.enums.EstadoMexicano;
import ipn.mx.isc.sismosapp.backend.mapper.SismoMapper;
import ipn.mx.isc.sismosapp.backend.model.Sismo;
import ipn.mx.isc.sismosapp.backend.repository.SismoRepository;
import ipn.mx.isc.sismosapp.backend.specification.SismoSpecification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    /**
     * Filtra sismos según criterios dinámicos
     * Si no se proporcionan filtros, devuelve los 100 más recientes
     * 
     * @param filters Criterios de filtrado (todos opcionales)
     * @return Lista de sismos que cumplen los criterios
     */
    public List<SismoDTO> filtrarSismos(SismoFilterDTO filters) {
        // Validar parámetros de paginación
        int page = (filters.getPage() != null && filters.getPage() >= 0) ? filters.getPage() : 0;
        int size = (filters.getSize() != null && filters.getSize() > 0) ? filters.getSize() : 100;
        
        // Validar rangos coherentes
        validarFiltros(filters);
        
        // Construir especificación dinámica
        Specification<Sismo> spec = SismoSpecification.withFilters(filters);
        
        // Aplicar paginación y ordenamiento
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaHora"));
        
        // Ejecutar consulta
        var resultado = sismoRepository.findAll(spec, pageable);
        
        logger.info("Filtrado de sismos: {} resultados encontrados (página {}, tamaño {})", 
            resultado.getTotalElements(), page, size);
        
        return sismoMapper.toDTOList(resultado.getContent());
    }

    /**
     * Valida que los filtros sean coherentes
     * @throws IllegalArgumentException si hay inconsistencias
     */
    private void validarFiltros(SismoFilterDTO filters) {
        // Validar rango de magnitud
        if (filters.getMagnitudMin() != null && filters.getMagnitudMax() != null) {
            if (filters.getMagnitudMin() > filters.getMagnitudMax()) {
                throw new IllegalArgumentException(
                    "La magnitud mínima no puede ser mayor que la máxima"
                );
            }
        }
        
        // Validar rango de fechas
        if (filters.getFechaInicio() != null && filters.getFechaFin() != null) {
            if (filters.getFechaInicio().isAfter(filters.getFechaFin())) {
                throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
                );
            }
        }
        
        // Validar rango de profundidad
        if (filters.getProfundidadMin() != null && filters.getProfundidadMax() != null) {
            if (filters.getProfundidadMin() > filters.getProfundidadMax()) {
                throw new IllegalArgumentException(
                    "La profundidad mínima no puede ser mayor que la máxima"
                );
            }
        }
        
        // Validar valores negativos
        if (filters.getMagnitudMin() != null && filters.getMagnitudMin() < 0) {
            throw new IllegalArgumentException("La magnitud mínima no puede ser negativa");
        }
        if (filters.getProfundidadMin() != null && filters.getProfundidadMin() < 0) {
            throw new IllegalArgumentException("La profundidad mínima no puede ser negativa");
        }
        
        // Validar que el estado exista en el catálogo
        if (filters.getEstado() != null && !filters.getEstado().trim().isEmpty()) {
            if (EstadoMexicano.fromNombreCompleto(filters.getEstado()).isEmpty()) {
                throw new IllegalArgumentException(
                    "Estado inválido. Debe ser uno de los estados de la República Mexicana"
                );
            }
        }
    }
}
