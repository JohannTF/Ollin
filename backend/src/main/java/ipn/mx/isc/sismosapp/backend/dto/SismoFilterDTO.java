package ipn.mx.isc.sismosapp.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO para filtrado dinámico de sismos
 * Todos los campos son opcionales y se pueden combinar entre sí
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Criterios de filtrado para sismos - todos los campos son opcionales")
public class SismoFilterDTO {
    
    @Schema(description = "Magnitud mínima del sismo", example = "3.0")
    private Double magnitudMin;
    
    @Schema(description = "Magnitud máxima del sismo", example = "7.0")
    private Double magnitudMax;
    
    @Schema(description = "Fecha y hora de inicio del rango de búsqueda", example = "2024-01-01T00:00:00Z")
    private OffsetDateTime fechaInicio;
    
    @Schema(description = "Fecha y hora de fin del rango de búsqueda", example = "2024-12-31T23:59:59Z")
    private OffsetDateTime fechaFin;
    
    @Schema(description = "Estado de la República donde ocurrió el sismo", example = "Guerrero")
    private String estado;
    
    @Schema(description = "Profundidad mínima en kilómetros", example = "0.0")
    private Double profundidadMin;
    
    @Schema(description = "Profundidad máxima en kilómetros", example = "100.0")
    private Double profundidadMax;
    
    @Schema(description = "Número de página (inicia en 0)", example = "0")
    private Integer page = 0;
    
    @Schema(description = "Cantidad de resultados por página", example = "100")
    private Integer size = 100;
}
