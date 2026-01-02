package ipn.mx.isc.frontend

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.messaging.FirebaseMessaging
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.DataSource
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import okio.BufferedSource

class MyApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Registrar token FCM al inicio
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { token ->
                    ipn.mx.isc.frontend.notification.NotificationRegistrar.registerToken(token)
                }
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Usar Decoder en lugar de Fetcher
                add(Base64Decoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}

/**
 * Decoder personalizado para manejar imágenes Base64
 */
class Base64Decoder(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult {
        return try {
            // Leer el contenido como string
            val bufferedSource: BufferedSource = source.source()
            val dataUri = bufferedSource.readUtf8()

            // Extraer datos Base64
            val base64Data = if (dataUri.contains(",")) {
                dataUri.split(",")[1]
            } else {
                dataUri
            }

            // Decodificar Base64 a bytes
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)

            // Convertir bytes a Bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Failed to decode bitmap from Base64 data")

            DecodeResult(
                drawable = bitmap.toDrawable(options.context.resources),
                isSampled = false
            )
        } catch (e: Exception) {
            throw e
        } finally {
            source.close()
        }
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            // Solo manejar si parece ser una imagen Base64
            val bufferedSource = result.source.source()

            return try {
                // Leer los primeros caracteres para verificar
                val preview = bufferedSource.peek().readUtf8(100)

                if (preview.startsWith("data:image/") && preview.contains(";base64,")) {
                    Base64Decoder(result.source, options)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

// Extensión para convertir Bitmap a Drawable
private fun android.graphics.Bitmap.toDrawable(resources: android.content.res.Resources): android.graphics.drawable.BitmapDrawable {
    return android.graphics.drawable.BitmapDrawable(resources, this)
}