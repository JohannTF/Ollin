package ipn.mx.isc.frontend.utils

import androidx.compose.ui.graphics.Color

object SismoColorUtils {
    
    /**
     * Retorna el color según la escala de magnitud del Servicio Sismológico Nacional
     */
    fun getColorForMagnitud(magnitud: Double): Color {
        return when {
            magnitud < 3.0 -> Color(0xFF00FF00)      // Verde - Menor a 3.0
            magnitud < 4.0 -> Color(0xFFFFFF00)      // Amarillo - Entre 3.0 y 3.9
            magnitud < 5.0 -> Color(0xFFFFA500)      // Naranja - Entre 4.0 y 4.9
            magnitud < 6.0 -> Color(0xFFFF8C00)      // Naranja oscuro - Entre 5.0 y 5.9
            magnitud < 7.0 -> Color(0xFFFF4500)      // Rojo naranja - Entre 6.0 y 6.9
            magnitud < 8.0 -> Color(0xFFFF0000)      // Rojo - Entre 7.0 y 7.9
            else -> Color(0xFF8B0000)                // Rojo oscuro - Mayor a 8.0
        }
    }
    
    /**
     * Retorna el tamaño del marcador según la magnitud (más grande = más fuerte)
     */
    fun getSizeForMagnitud(magnitud: Double): Float {
        return when {
            magnitud < 3.0 -> 8f
            magnitud < 4.0 -> 12f
            magnitud < 5.0 -> 16f
            magnitud < 6.0 -> 20f
            magnitud < 7.0 -> 24f
            magnitud < 8.0 -> 28f
            else -> 32f
        }
    }
    
    /**
     * Retorna una descripción de la categoría de magnitud
     */
    fun getCategoriaDescripcion(magnitud: Double): String {
        return when {
            magnitud < 3.0 -> "Menor"
            magnitud < 4.0 -> "Ligero"
            magnitud < 5.0 -> "Moderado"
            magnitud < 6.0 -> "Fuerte"
            magnitud < 7.0 -> "Muy Fuerte"
            magnitud < 8.0 -> "Mayor"
            else -> "Gran Terremoto"
        }
    }
}
