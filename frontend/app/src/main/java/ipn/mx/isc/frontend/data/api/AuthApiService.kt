package ipn.mx.isc.frontend.data.api

import ipn.mx.isc.frontend.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/auth/registro")
    suspend fun registro(@Body request: RegistroRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/perfil/{usuarioId}")
    suspend fun obtenerPerfil(@Path("usuarioId") usuarioId: String): Response<Usuario>

    @PUT("api/auth/perfil/{usuarioId}")
    suspend fun actualizarPerfil(
        @Path("usuarioId") usuarioId: String,
        @Body request: ActualizarPerfilRequest
    ): Response<Usuario>

    @PUT("api/auth/cambiar-password/{usuarioId}")
    suspend fun cambiarPassword(
        @Path("usuarioId") usuarioId: String,
        @Body request: CambiarPasswordRequest
    ): Response<AuthResponse>
}