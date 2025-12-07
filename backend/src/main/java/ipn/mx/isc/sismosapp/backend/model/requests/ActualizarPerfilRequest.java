package ipn.mx.isc.sismosapp.backend.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilRequest {
    private String nombre;
    private String imagenPerfilUrl;
}