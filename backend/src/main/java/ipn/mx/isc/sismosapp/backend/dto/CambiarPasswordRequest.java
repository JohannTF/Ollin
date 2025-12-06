package ipn.mx.isc.sismosapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordRequest {
    private String contrasenaActual;
    private String contrasenaNueva;
}