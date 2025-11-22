package ipn.mx.isc.frontend.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import ipn.mx.isc.frontend.data.model.Sismo
import ipn.mx.isc.frontend.utils.SismoColorUtils
import ipn.mx.isc.frontend.utils.createCircleBitmap

@Composable
fun SismoMarker(
    sismo: Sismo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val position = remember(sismo) {
        LatLng(sismo.latitud, sismo.longitud)
    }
    
    val color = SismoColorUtils.getColorForMagnitud(sismo.magnitud)
    val baseSizeDp = SismoColorUtils.getSizeForMagnitud(sismo.magnitud)
    
    // Animación de escala cuando está seleccionado
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.5f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "markerScale"
    )
    
    val sizeDp = baseSizeDp * scale
    val density = LocalDensity.current
    val diameterPx = remember(sizeDp) { with(density) { sizeDp.dp.toPx().toInt() } }

    val bmp = remember(sismo.magnitud, diameterPx) {
        // convert Compose Color to Android int
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
