package ipn.mx.isc.sismosapp.backend.model.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para crear manualmente un sismo (fuente distinta a SSN)")
public class SismoRequest {

    @NotNull
    @Schema(description = "Fecha y hora del evento", example = "2024-11-22T14:30:00Z")
    private OffsetDateTime fechaHora;

    @NotNull
    @Schema(description = "Latitud del epicentro", example = "18.5")
    private Double latitud;

    @NotNull
    @Schema(description = "Longitud del epicentro", example = "-97.2")
    private Double longitud;

    @NotNull
    @Schema(description = "Magnitud del sismo", example = "4.2")
    private Double magnitud;

    @NotNull
    @Schema(description = "Profundidad en km", example = "33.0")
    private Double profundidadKm;

    @NotBlank
    @Schema(description = "Descripción de la ubicación", example = "12 km al suroeste de Acapulco, Guerrero")
    private String lugar;

    @Schema(description = "Fuente de datos (se sobrescribe si es SSN)", example = "Personal")
    private String fuente;
}
