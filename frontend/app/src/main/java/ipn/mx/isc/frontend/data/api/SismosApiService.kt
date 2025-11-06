package ipn.mx.isc.frontend.data.api

import ipn.mx.isc.frontend.data.model.Sismo
import retrofit2.http.GET
import retrofit2.http.Query

interface SismosApiService {
    
    @GET("api/sismos")
    suspend fun obtenerSismos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): List<Sismo>
    
    @GET("api/sismos/recientes")
    suspend fun obtenerSismosRecientes(
        @Query("horas") horas: Int = 24
    ): List<Sismo>
    
    @GET("api/sismos/magnitud")
    suspend fun obtenerSismosPorMagnitud(
        @Query("minima") minima: Double = 3.0
    ): List<Sismo>
}
