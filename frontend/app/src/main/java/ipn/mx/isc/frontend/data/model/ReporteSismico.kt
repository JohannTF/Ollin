package ipn.mx.isc.frontend.data.model

import com.google.gson.annotations.SerializedName

data class ReporteSismico(
    @SerializedName("tipoReporte")
    val tipoReporte: String,
    
    @SerializedName("fechaInicio")
    val fechaInicio: String,
    
    @SerializedName("fechaFin")
    val fechaFin: String,
    
    @SerializedName("fechaGeneracion")
    val fechaGeneracion: String,
    
    @SerializedName("totalSismos")
    val totalSismos: Long,
    
    @SerializedName("magnitudPromedio")
    val magnitudPromedio: Double,
    
    @SerializedName("magnitudMaxima")
    val magnitudMaxima: Double,
    
    @SerializedName("magnitudMinima")
    val magnitudMinima: Double,
    
    @SerializedName("profundidadPromedio")
    val profundidadPromedio: Double,
    
    @SerializedName("profundidadMaxima")
    val profundidadMaxima: Double,
    
    @SerializedName("profundidadMinima")
    val profundidadMinima: Double,
    
    @SerializedName("distribucionPorMagnitud")
    val distribucionPorMagnitud: Map<String, Long>,
    
    @SerializedName("distribucionPorEstado")
    val distribucionPorEstado: Map<String, Long>,
    
    @SerializedName("distribucionPorMes")
    val distribucionPorMes: Map<String, Long>,
    
    @SerializedName("sismosMasFuertes")
    val sismosMasFuertes: List<SismoResumen>,
    
    @SerializedName("sismosPorDia")
    val sismosPorDia: Map<String, Long>
)

data class SismoResumen(
    @SerializedName("fecha")
    val fecha: String,
    
    @SerializedName("hora")
    val hora: String,
    
    @SerializedName("magnitud")
    val magnitud: Double,
    
    @SerializedName("ubicacion")
    val ubicacion: String,
    
    @SerializedName("profundidad")
    val profundidad: Double
)