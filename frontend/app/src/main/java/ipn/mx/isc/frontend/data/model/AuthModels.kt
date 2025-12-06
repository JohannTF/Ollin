package ipn.mx.isc.frontend.data.model

// Request para registro
data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String
)

// Request para login
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

// Response de autenticación
data class AuthResponse(
    val id: String?,
    val nombre: String?,
    val correo: String?,
    val imagenPerfilUrl: String?,
    val mensaje: String
)

// Request para actualizar perfil
data class ActualizarPerfilRequest(
    val nombre: String? = null,
    val imagenPerfilUrl: String? = null
)

// Request para cambiar contraseña
data class CambiarPasswordRequest(
    val contrasenaActual: String,
    val contrasenaNueva: String
)

// Usuario actualizado para coincidir con backend
data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val imagenPerfilUrl: String? = null
)