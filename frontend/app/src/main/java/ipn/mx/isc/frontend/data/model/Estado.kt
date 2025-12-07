package ipn.mx.isc.frontend.data.model

/**
 * Catálogo de estados de la República Mexicana
 * Incluye abreviaturas según el Servicio Sismológico Nacional (SSN)
 */
enum class EstadoMexicano(
    val nombreCompleto: String,
    val abreviatura: String
) {
    AGUASCALIENTES("Aguascalientes", "AGS"),
    BAJA_CALIFORNIA("Baja California", "BC"),
    BAJA_CALIFORNIA_SUR("Baja California Sur", "BCS"),
    CAMPECHE("Campeche", "CAMP"),
    CHIAPAS("Chiapas", "CHIS"),
    CHIHUAHUA("Chihuahua", "CHIH"),
    CIUDAD_DE_MEXICO("Ciudad de México", "CDMX"),
    COAHUILA("Coahuila", "COAH"),
    COLIMA("Colima", "COL"),
    DURANGO("Durango", "DGO"),
    ESTADO_DE_MEXICO("Estado de México", "MEX"),
    GUANAJUATO("Guanajuato", "GTO"),
    GUERRERO("Guerrero", "GRO"),
    HIDALGO("Hidalgo", "HGO"),
    JALISCO("Jalisco", "JAL"),
    MICHOACAN("Michoacán", "MICH"),
    MORELOS("Morelos", "MOR"),
    NAYARIT("Nayarit", "NAY"),
    NUEVO_LEON("Nuevo León", "NL"),
    OAXACA("Oaxaca", "OAX"),
    PUEBLA("Puebla", "PUE"),
    QUERETARO("Querétaro", "QRO"),
    QUINTANA_ROO("Quintana Roo", "QROO"),
    SAN_LUIS_POTOSI("San Luis Potosí", "SLP"),
    SINALOA("Sinaloa", "SIN"),
    SONORA("Sonora", "SON"),
    TABASCO("Tabasco", "TAB"),
    TAMAULIPAS("Tamaulipas", "TAMPS"),
    TLAXCALA("Tlaxcala", "TLAX"),
    VERACRUZ("Veracruz", "VER"),
    YUCATAN("Yucatán", "YUC"),
    ZACATECAS("Zacatecas", "ZAC");

    companion object {
        /**
         * Obtiene la lista completa de estados ordenada alfabéticamente
         */
        fun obtenerTodos(): List<EstadoMexicano> {
            return values().sortedBy { it.nombreCompleto }
        }

        /**
         * Busca un estado por su nombre completo
         */
        fun fromNombreCompleto(nombre: String): EstadoMexicano? {
            return values().find { it.nombreCompleto.equals(nombre, ignoreCase = true) }
        }
    }
}
