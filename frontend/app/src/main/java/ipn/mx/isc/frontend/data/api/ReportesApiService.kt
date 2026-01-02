package ipn.mx.isc.frontend.data.api

import ipn.mx.isc.frontend.data.model.ReporteSismico
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ReportesApiService {
    
    // Endpoints JSON
    @GET("api/reportes/trimestral")
    suspend fun obtenerReporteTrimestral(): Response<ReporteSismico>
    
    @GET("api/reportes/semestral")
    suspend fun obtenerReporteSemestral(): Response<ReporteSismico>
    
    @GET("api/reportes/anual")
    suspend fun obtenerReporteAnual(): Response<ReporteSismico>
    
    
    @GET("api/reportes/trimestral/pdf")
    suspend fun descargarReporteTrimestralPdf(): Response<ResponseBody>
    
    @GET("api/reportes/semestral/pdf")
    suspend fun descargarReporteSemestralPdf(): Response<ResponseBody>
    
    @GET("api/reportes/anual/pdf")
    suspend fun descargarReporteAnualPdf(): Response<ResponseBody>
}