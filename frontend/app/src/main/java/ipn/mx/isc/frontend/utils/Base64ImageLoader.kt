// Crea: Base64ImageLoader.kt
package ipn.mx.isc.frontend.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter

object Base64ImageLoader {

    fun decode(base64String: String): Bitmap? {
        return try {
            Log.d("Base64ImageLoader", "Decoding Base64 image")

            val base64Data = if (base64String.contains(",")) {
                base64String.split(",")[1]
            } else {
                base64String
            }

            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            Log.d("Base64ImageLoader", "Successfully decoded: ${bitmap?.width}x${bitmap?.height}")
            bitmap
        } catch (e: Exception) {
            Log.e("Base64ImageLoader", "Error decoding Base64", e)
            null
        }
    }
}

@Composable
fun rememberBase64Painter(base64String: String?): Painter? {
    return remember(base64String) {
        base64String?.let {
            Base64ImageLoader.decode(it)?.asImageBitmap()?.let { imageBitmap ->
                BitmapPainter(imageBitmap)
            }
        }
    }
}