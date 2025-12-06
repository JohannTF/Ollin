package ipn.mx.isc.frontend

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
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
        Log.d("MyApplication", "========================================")
        Log.d("MyApplication", "APPLICATION INITIALIZED WITH BASE64 SUPPORT")
        Log.d("MyApplication", "========================================")
    }

    override fun newImageLoader(): ImageLoader {
        Log.d("MyApplication", "Creating ImageLoader with Base64Decoder")

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
 * Este enfoque es más confiable que usar un Fetcher
 */
class Base64Decoder(
    private val source: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult {
        return try {
            Log.d("Base64Decoder", "========================================")
            Log.d("Base64Decoder", "DECODING BASE64 IMAGE")

            // Leer el contenido como string
            val bufferedSource: BufferedSource = source.source()
            val dataUri = bufferedSource.readUtf8()

            Log.d("Base64Decoder", "Data URI length: ${dataUri.length}")
            Log.d("Base64Decoder", "Data URI prefix: ${dataUri.take(50)}")

            // Extraer datos Base64
            val base64Data = if (dataUri.contains(",")) {
                dataUri.split(",")[1]
            } else {
                dataUri
            }

            // Decodificar Base64 a bytes
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            Log.d("Base64Decoder", "Decoded ${bytes.size} bytes")

            // Convertir bytes a Bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Failed to decode bitmap from Base64 data")

            Log.d("Base64Decoder", "Bitmap created: ${bitmap.width}x${bitmap.height}")
            Log.d("Base64Decoder", "========================================")

            DecodeResult(
                drawable = bitmap.toDrawable(options.context.resources),
                isSampled = false
            )
        } catch (e: Exception) {
            Log.e("Base64Decoder", "Error decoding Base64 image", e)
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

                Log.d("Base64DecoderFactory", "Checking source: ${preview.take(50)}")

                if (preview.startsWith("data:image/") && preview.contains(";base64,")) {
                    Log.d("Base64DecoderFactory", ">>> CREATING Base64Decoder <<<")
                    Base64Decoder(result.source, options)
                } else {
                    Log.d("Base64DecoderFactory", "Not a Base64 data URI")
                    null
                }
            } catch (e: Exception) {
                Log.e("Base64DecoderFactory", "Error checking source", e)
                null
            }
        }
    }
}

// Extensión para convertir Bitmap a Drawable
private fun android.graphics.Bitmap.toDrawable(resources: android.content.res.Resources): android.graphics.drawable.BitmapDrawable {
    return android.graphics.drawable.BitmapDrawable(resources, this)
}