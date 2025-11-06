package ipn.mx.isc.frontend.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt

/**
 * Genera un bitmap circular con borde blanco usado para marcadores.
 */
fun createCircleBitmap(diameterPx: Int, @ColorInt fillColor: Int, strokePx: Int = 2, @ColorInt strokeColor: Int = 0xFFFFFFFF.toInt()): Bitmap {
    val bitmap = Bitmap.createBitmap(diameterPx, diameterPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor }
    val radius = diameterPx / 2f
    canvas.drawCircle(radius, radius, radius - strokePx, paint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokePx.toFloat()
        color = strokeColor
    }
    canvas.drawCircle(radius, radius, radius - strokePx, strokePaint)
    return bitmap
}
