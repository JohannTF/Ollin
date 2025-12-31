package ipn.mx.isc.frontend.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import ipn.mx.isc.frontend.utils.createCircleBitmap

/**
 * Marcador de sismo
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
    val density = LocalDensity.current
    
    // Animación de tamaño cuando está seleccionado
    val infiniteTransition = rememberInfiniteTransition(label = "sizeAnimation")
    val sizeFactor = if (isSelected) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulsing_size"
        ).value
    } else {
        1f
    }
    
    val animatedSize = baseSize * sizeFactor
    val diameterPx = remember(animatedSize) { 
        with(density) { animatedSize.dp.toPx().toInt() } 
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
