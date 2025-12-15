package ipn.mx.isc.frontend.data.api

import ipn.mx.isc.frontend.data.model.DeviceTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApiService {
    @POST("api/notifications/register")
    suspend fun registrarToken(@Body request: DeviceTokenRequest)
}
