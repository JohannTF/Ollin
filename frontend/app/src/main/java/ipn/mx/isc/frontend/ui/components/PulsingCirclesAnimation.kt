package ipn.mx.isc.frontend.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Animación de círculos pulsantes para indicar sismo seleccionado
 * 
 * @param color Color base de la animación
 * @param baseSize Tamaño base del círculo central en dp
 */
@Composable
fun PulsingCirclesAnimation(
    color: Color,
    baseSize: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingTransition")
    val sizes = listOf(baseSize, baseSize * 1.5f, baseSize * 2f)

    val scaleFactors = List(sizes.size) { index ->
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2000
                    1f at 0 using FastOutSlowInEasing
                    1.5f at 500 using FastOutSlowInEasing
                    2.0f at 1000 using FastOutSlowInEasing
                    2.5f at 2000 using FastOutSlowInEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 666)
            ),
            label = "scale_$index"
        )
    }

    val alphaValues = List(sizes.size) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2000
                    0.7f at 0 using LinearEasing
                    0.5f at 500 using LinearEasing
                    0.3f at 1000 using LinearEasing
                    0.1f at 1500 using LinearEasing
                    0f at 2000 using LinearEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 666)
            ),
            label = "alpha_$index"
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dibujar círculos pulsantes
            sizes.forEachIndexed { index, size ->
                val scale = scaleFactors[index].value
                val alpha = alphaValues[index].value
                val radius = size.dp.toPx() / 2f
                drawCircle(
                    color = color,
                    radius = radius * scale,
                    center = center,
                    alpha = alpha
                )
            }

            // Círculo estático central (marcador del sismo)
            drawCircle(
                color = color,
                radius = baseSize.dp.toPx() / 2f,
                center = center,
                alpha = 1f
            )
            
            // Borde blanco del círculo central
            drawCircle(
                color = Color.White,
                radius = baseSize.dp.toPx() / 2f,
                center = center,
                alpha = 1f,
                style = Stroke(width = 4f)
            )
        }
    }
}