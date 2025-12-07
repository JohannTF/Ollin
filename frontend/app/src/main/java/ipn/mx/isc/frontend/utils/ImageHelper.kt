package ipn.mx.isc.frontend.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageHelper {

    /**
     * Convierte una URI de imagen a Base64 (Data URI)
     * Comprime la imagen automáticamente y corrige la orientación
     */
    fun uriToBase64(context: Context, uri: Uri, maxSizeKB: Int = 500): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Corregir orientación EXIF
            bitmap = correctImageOrientation(context, uri, bitmap)

            // Comprimir imagen
            val compressedBitmap = compressBitmap(bitmap, maxSizeKB)

            // Convertir a Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // Crear Data URI con el formato correcto
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Corrige la orientación de la imagen según los datos EXIF
     */
    private fun correctImageOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Comprime un bitmap hasta que sea menor al tamaño especificado
     */
    private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height

        // Reducir dimensiones si es muy grande (máximo 1024px en el lado más largo)
        val maxDimension = 1024
        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            if (width > height) {
                width = maxDimension
                height = (maxDimension / ratio).toInt()
            } else {
                height = maxDimension
                width = (maxDimension * ratio).toInt()
            }
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Valida si una cadena es una imagen Base64 válida
     * Verifica formato completo: data:image/[tipo];base64,[datos]
     */
    fun isValidBase64Image(base64String: String?): Boolean {
        if (base64String.isNullOrEmpty()) return false

        // Debe empezar con data:image/
        if (!base64String.startsWith("data:image/")) return false

        // Debe contener ;base64,
        if (!base64String.contains(";base64,")) return false

        // Verificar que tiene datos después de la coma
        val parts = base64String.split(",")
        if (parts.size != 2) return false

        // Verificar que la parte Base64 no esté vacía
        val base64Data = parts[1]
        if (base64Data.isEmpty()) return false

        // Verificar formato del header
        val header = parts[0]
        val validFormats = listOf("data:image/jpeg", "data:image/jpg", "data:image/png", "data:image/gif")
        return validFormats.any { header.startsWith(it) }
    }

    /**
     * Obtiene el tamaño aproximado en KB de una imagen Base64
     */
    fun getBase64SizeKB(base64String: String): Int {
        val base64Data = if (base64String.contains(",")) {
            base64String.split(",")[1]
        } else {
            base64String
        }
        return (base64Data.length * 3 / 4) / 1024
    }

    /**
     * Extrae solo los datos Base64 (sin el prefijo data:image/...)
     * Útil para debugging o procesamiento
     */
    fun extractBase64Data(dataUri: String): String? {
        if (!dataUri.contains(",")) return null
        return dataUri.split(",").getOrNull(1)
    }

    /**
     * Obtiene el tipo MIME de una imagen Base64
     */
    fun getMimeType(dataUri: String): String? {
        if (!dataUri.startsWith("data:")) return null
        val header = dataUri.split(",").firstOrNull() ?: return null
        return header.replace("data:", "").replace(";base64", "")
    }
}