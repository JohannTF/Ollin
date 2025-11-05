package ipn.mx.isc.sismosapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SismoDTO {
    private String id;
    private OffsetDateTime fechaHora;
    private Double latitud;
    private Double longitud;
    private Double magnitud;
    private Double profundidadKm;
    private String lugar;
    private String fuente;
}
