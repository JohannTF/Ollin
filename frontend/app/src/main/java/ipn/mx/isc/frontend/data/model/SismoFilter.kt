package ipn.mx.isc.frontend.data.model

import java.time.OffsetDateTime

/**
 * Modelo para criterios de filtrado de sismos
 * Todos los campos son opcionales y se pueden combinar
 */
data class SismoFilter(
    val magnitudMin: Double? = null,
    val magnitudMax: Double? = null,
    val fechaInicio: String? = null,  // ISO 8601 format
    val fechaFin: String? = null,      // ISO 8601 format
    val estado: String? = null,
    val profundidadMin: Double? = null,
    val profundidadMax: Double? = null,
    val page: Int = 0,
    val size: Int = 100
) {
    /**
     * Verifica si hay algún filtro activo (diferente de los valores por defecto)
     */
    fun tieneFiltrosActivos(): Boolean {
        return magnitudMin != null ||
               magnitudMax != null ||
               fechaInicio != null ||
               fechaFin != null ||
               estado?.isNotBlank() == true ||
               profundidadMin != null ||
               profundidadMax != null
    }

    companion object {
        /**
         * Filtro vacío con valores por defecto
         */
        fun vacio() = SismoFilter()
    }
}
