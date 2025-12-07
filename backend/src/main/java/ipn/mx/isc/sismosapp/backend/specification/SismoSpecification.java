package ipn.mx.isc.sismosapp.backend.specification;

import ipn.mx.isc.sismosapp.backend.model.dto.SismoFilterDTO;
import ipn.mx.isc.sismosapp.backend.model.entities.Sismo;
import ipn.mx.isc.sismosapp.backend.model.enums.EstadoMexicano;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones para construir consultas dinámicas de sismos
 * Permite combinar múltiples criterios de filtrado
 */
public class SismoSpecification {

    /**
     * Construye una especificación dinámica basada en los filtros proporcionados
     * Solo incluye criterios que no sean nulos
     */
    public static Specification<Sismo> withFilters(SismoFilterDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por magnitud mínima
            if (filters.getMagnitudMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("magnitud"), filters.getMagnitudMin()
                ));
            }
            
            // Filtro por magnitud máxima
            if (filters.getMagnitudMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("magnitud"), filters.getMagnitudMax()
                ));
            }
            
            // Filtro por fecha de inicio
            if (filters.getFechaInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("fechaHora"), filters.getFechaInicio()
                ));
            }
            
            // Filtro por fecha de fin
            if (filters.getFechaFin() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("fechaHora"), filters.getFechaFin()
                ));
            }
            
            // Filtro por estado (búsqueda por abreviatura oficial al final del campo)
            // Formato esperado: "XX km al DIRECCION de CIUDAD, ESTADO"
            if (filters.getEstado() != null && !filters.getEstado().trim().isEmpty()) {
                EstadoMexicano.fromNombreCompleto(filters.getEstado())
                    .ifPresent(estado -> {
                        predicates.add(criteriaBuilder.like(
                            criteriaBuilder.upper(root.get("lugar")),
                            "%, " + estado.getAbreviatura().toUpperCase()
                        ));
                    });
            }
            
            // Filtro por profundidad mínima
            if (filters.getProfundidadMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("profundidadKm"), filters.getProfundidadMin()
                ));
            }
            
            // Filtro por profundidad máxima
            if (filters.getProfundidadMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("profundidadKm"), filters.getProfundidadMax()
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
