package ipn.mx.isc.frontend.data.api

import ipn.mx.isc.frontend.data.model.Sismo
import ipn.mx.isc.frontend.data.model.SismoFilter
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SismosApiService {
    
    @GET("api/sismos")
    suspend fun obtenerSismos(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): List<Sismo>
    
    @POST("api/sismos/filtrar")
    suspend fun filtrarSismos(
        @Body filtros: SismoFilter
    ): List<Sismo>
}
