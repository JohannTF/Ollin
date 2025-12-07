package ipn.mx.isc.sismosapp.backend.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String id;
    private String nombre;
    private String correo;
    private String imagenPerfilUrl;
    private String mensaje;
}