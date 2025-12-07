package ipn.mx.isc.frontend.data.repository

import android.content.Context
import android.content.SharedPreferences
import ipn.mx.isc.frontend.data.api.RetrofitClient
import ipn.mx.isc.frontend.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AuthRepository(context: Context) {

    private val apiService = RetrofitClient.authApiService
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NOMBRE = "user_nombre"
        private const val KEY_USER_CORREO = "user_correo"
        private const val KEY_USER_IMAGEN = "user_imagen"
    }

    suspend fun login(correo: String, contrasena: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(correo, contrasena))

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.id != null) {
                    val usuario = Usuario(
                        id = authResponse.id,
                        nombre = authResponse.nombre ?: "",
                        correo = authResponse.correo ?: correo,
                        imagenPerfilUrl = authResponse.imagenPerfilUrl
                    )
                    saveUserSession(usuario)
                    Result.success(usuario)
                } else {
                    Result.failure(Exception(authResponse?.mensaje ?: "Error al iniciar sesión"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBody)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registro(nombre: String, correo: String, contrasena: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registro(RegistroRequest(nombre, correo, contrasena))

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.id != null) {
                    val usuario = Usuario(
                        id = authResponse.id,
                        nombre = authResponse.nombre ?: nombre,
                        correo = authResponse.correo ?: correo,
                        imagenPerfilUrl = authResponse.imagenPerfilUrl
                    )
                    saveUserSession(usuario)
                    Result.success(usuario)
                } else {
                    Result.failure(Exception(authResponse?.mensaje ?: "Error al registrarse"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBody)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarPerfil(usuarioId: String, nombre: String, imagenUrl: String?): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val request = ActualizarPerfilRequest(nombre, imagenUrl)
            val response = apiService.actualizarPerfil(usuarioId, request)

            if (response.isSuccessful) {
                val usuario = response.body()
                if (usuario != null) {
                    saveUserSession(usuario)
                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Error al actualizar perfil"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBody)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cambiarPassword(usuarioId: String, contrasenaActual: String, contrasenaNueva: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = CambiarPasswordRequest(contrasenaActual, contrasenaNueva)
            val response = apiService.cambiarPassword(usuarioId, request)

            if (response.isSuccessful) {
                Result.success(response.body()?.mensaje ?: "Contraseña actualizada")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = extractErrorMessage(errorBody)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSavedUser(): Usuario? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val nombre = prefs.getString(KEY_USER_NOMBRE, null) ?: return null
        val correo = prefs.getString(KEY_USER_CORREO, null) ?: return null
        val imagen = prefs.getString(KEY_USER_IMAGEN, null)

        return Usuario(userId, nombre, correo, imagen)
    }

    private fun saveUserSession(usuario: Usuario) {
        prefs.edit().apply {
            putString(KEY_USER_ID, usuario.id)
            putString(KEY_USER_NOMBRE, usuario.nombre)
            putString(KEY_USER_CORREO, usuario.correo)
            putString(KEY_USER_IMAGEN, usuario.imagenPerfilUrl)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Extrae el mensaje de error del JSON de respuesta
     */
    private fun extractErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Error en la operación"
        }

        return try {
            val json = JSONObject(errorBody)
            json.optString("mensaje", "Error en la operación")
        } catch (e: Exception) {
            // Si no es JSON válido, retornar mensaje genérico
            "Error en la operación"
        }
    }
}