package ipn.mx.isc.frontend.data.model

import com.google.gson.annotations.SerializedName

data class Sismo(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("fechaHora")
    val fechaHora: String, 
    
    @SerializedName("latitud")
    val latitud: Double,
    
    @SerializedName("longitud")
    val longitud: Double,
    
    @SerializedName("magnitud")
    val magnitud: Double,
    
    @SerializedName("profundidadKm")
    val profundidadKm: Double,
    
    @SerializedName("lugar")
    val lugar: String,
    
    @SerializedName("fuente")
    val fuente: String
)
