package ipn.mx.isc.sismosapp.backend.service;

import ipn.mx.isc.sismosapp.backend.dto.*;
import ipn.mx.isc.sismosapp.backend.model.Usuario;
import ipn.mx.isc.sismosapp.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public AuthResponse registrarUsuario(RegistroRequest request) {
        // Validar que el correo no exista
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            return new AuthResponse(null, null, null, null, "El correo ya está registrado");
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasenaHash(hashPassword(request.getContrasena()));
        usuario.setImagenPerfilUrl(null);

        // Guardar
        usuario = usuarioRepository.save(usuario);
        logger.info("Usuario registrado: {}", usuario.getCorreo());

        return new AuthResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getImagenPerfilUrl(),
            "Registro exitoso"
        );
    }

    public AuthResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(request.getCorreo());

        if (usuarioOpt.isEmpty()) {
            return new AuthResponse(null, null, null, null, "Correo o contraseña incorrectos");
        }

        Usuario usuario = usuarioOpt.get();
        String hashedPassword = hashPassword(request.getContrasena());

        if (!usuario.getContrasenaHash().equals(hashedPassword)) {
            return new AuthResponse(null, null, null, null, "Correo o contraseña incorrectos");
        }

        logger.info("Login exitoso: {}", usuario.getCorreo());

        return new AuthResponse(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getImagenPerfilUrl(),
            "Login exitoso"
        );
    }

    @Transactional
    public UsuarioDTO actualizarPerfil(String usuarioId, ActualizarPerfilRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        
        if (request.getNombre() != null && !request.getNombre().isEmpty()) {
            usuario.setNombre(request.getNombre());
        }
        
        if (request.getImagenPerfilUrl() != null) {
            usuario.setImagenPerfilUrl(request.getImagenPerfilUrl());
        }

        usuario = usuarioRepository.save(usuario);
        logger.info("Perfil actualizado: {}", usuario.getCorreo());

        return convertirADTO(usuario);
    }

    public UsuarioDTO obtenerPerfil(String usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        return usuarioOpt.map(this::convertirADTO).orElse(null);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error al hashear contraseña", e);
            throw new RuntimeException("Error al procesar contraseña");
        }
    }

    @Transactional
    public boolean cambiarPassword(String usuarioId, CambiarPasswordRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        String hashedCurrentPassword = hashPassword(request.getContrasenaActual());

        // Verificar que la contraseña actual sea correcta
        if (!usuario.getContrasenaHash().equals(hashedCurrentPassword)) {
            return false;
        }

        // Actualizar con la nueva contraseña
        usuario.setContrasenaHash(hashPassword(request.getContrasenaNueva()));
        usuarioRepository.save(usuario);
        logger.info("Contraseña actualizada para usuario: {}", usuario.getCorreo());

        return true;
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getImagenPerfilUrl()
        );
    }
}