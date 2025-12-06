package ipn.mx.isc.sismosapp.backend.controller;

import ipn.mx.isc.sismosapp.backend.dto.*;
import ipn.mx.isc.sismosapp.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@RequestBody RegistroRequest request) {
        AuthResponse response = authService.registrarUsuario(request);
        
        if (response.getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        if (response.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/perfil/{usuarioId}")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(@PathVariable String usuarioId) {
        UsuarioDTO usuario = authService.obtenerPerfil(usuarioId);
        
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/perfil/{usuarioId}")
    public ResponseEntity<UsuarioDTO> actualizarPerfil(
        @PathVariable String usuarioId,
        @RequestBody ActualizarPerfilRequest request
    ) {
        UsuarioDTO usuario = authService.actualizarPerfil(usuarioId, request);
        
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/cambiar-password/{usuarioId}")
    public ResponseEntity<AuthResponse> cambiarPassword(
        @PathVariable String usuarioId,
        @RequestBody CambiarPasswordRequest request
    ) {
        boolean exitoso = authService.cambiarPassword(usuarioId, request);
        
        if (!exitoso) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new AuthResponse(null, null, null, null, "Contraseña actual incorrecta"));
        }
        
        return ResponseEntity.ok(new AuthResponse(null, null, null, null, "Contraseña actualizada exitosamente"));
    }
}