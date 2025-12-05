package ipn.mx.isc.sismosapp.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de un evento sísmico registrado por el SSN")
public class SismoDTO {
    
    @Schema(description = "Identificador único del sismo", example = "2024-11-22T14:30:00Z_18.5_-97.2_4.2")
    private String id;
    
    @Schema(description = "Fecha y hora del evento sísmico en formato ISO 8601", example = "2024-11-22T14:30:00Z")
    private OffsetDateTime fechaHora;
    
    @Schema(description = "Latitud del epicentro en grados decimales", example = "18.5")
    private Double latitud;
    
    @Schema(description = "Longitud del epicentro en grados decimales", example = "-97.2")
    private Double longitud;
    
    @Schema(description = "Magnitud del sismo en escala de Richter", example = "4.2")
    private Double magnitud;
    
    @Schema(description = "Profundidad del hipocentro en kilómetros", example = "33.0")
    private Double profundidadKm;
    
    @Schema(description = "Descripción de la ubicación del sismo", example = "12 km al suroeste de Acapulco, Guerrero")
    private String lugar;
    
    @Schema(description = "Fuente de los datos", example = "SSN")
    private String fuente;
}
