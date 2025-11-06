package ipn.mx.isc.frontend.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import ipn.mx.isc.frontend.utils.SismoColorUtils
import ipn.mx.isc.frontend.utils.createCircleBitmap

@Composable
fun SismoMarker(
    position: LatLng,
    magnitud: Double,
    onClick: () -> Unit = {}
) {
    val color = SismoColorUtils.getColorForMagnitud(magnitud)
    val sizeDp = SismoColorUtils.getSizeForMagnitud(magnitud)
    val density = LocalDensity.current
    val diameterPx = remember(sizeDp) { with(density) { sizeDp.dp.toPx().toInt() } }

    val bmp = remember(magnitud, diameterPx) {
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
        title = "Magnitud: $magnitud",
        onClick = {
            onClick()
            true
        }
    )
}
