package ipn.mx.isc.frontend.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import ipn.mx.isc.frontend.utils.createCircleBitmap

/**
 * Marcador de sismo con animación de círculos pulsantes cuando está seleccionado
 * 
 * @param position Posición geográfica del sismo
 * @param color Color del sismo basado en su magnitud
 * @param baseSize Tamaño base del marcador en dp
 * @param isSelected Indica si el sismo está seleccionado
 * @param onClick Callback cuando se hace clic en el marcador
 */
@Composable
fun PulsingSismoMarker(
    position: LatLng,
    color: Color,
    baseSize: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        // Usar MarkerComposable para mostrar animación
        MarkerComposable(
            state = MarkerState(position),
            anchor = Offset(0.5f, 0.5f),
            onClick = {
                onClick()
                true
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size((baseSize * 8).dp)
            ) {
                // Animación de círculos pulsantes
                PulsingCirclesAnimation(
                    color = color,
                    baseSize = baseSize
                )
            }
        }
    } else {
        // Marcador normal con bitmap (más eficiente)
        val density = LocalDensity.current
        val diameterPx = remember(baseSize) { 
            with(density) { baseSize.dp.toPx().toInt() } 
        }

        val bmp = remember(color, diameterPx) {
            val androidColor = AndroidColor.argb(
                (color.alpha * 255).toInt(),
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            createCircleBitmap(diameterPx, androidColor, strokePx = 2)
        }

        Marker(
            state = MarkerState(position),
            icon = BitmapDescriptorFactory.fromBitmap(bmp),
            anchor = Offset(0.5f, 0.5f),
            onClick = {
                onClick()
                true
            }
        )
    }
}
