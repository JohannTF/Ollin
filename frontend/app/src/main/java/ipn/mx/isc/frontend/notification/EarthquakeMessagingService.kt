package ipn.mx.isc.frontend.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ipn.mx.isc.frontend.data.model.Sismo
import ipn.mx.isc.frontend.notification.NotificationUtils.showNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EarthquakeMessagingService : FirebaseMessagingService() {

    private val gson = Gson()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        NotificationRegistrar.registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Manejar mensajes data (nuevos sismos en tiempo real)
        if (message.data.isNotEmpty() && message.data.containsKey("sismos")) {
            handleSismosDataMessage(message.data["sismos"] ?: "")
            return
        }

        // Manejar notificaciones de sismos críticos
        if (message.notification != null) {
            handleNotificationMessage(message)
        }
    }

    /**
     * Procesa mensajes data con lista de sismos
     * Estructura: {"sismos": "[{...}, {...}]"}
     */
    private fun handleSismosDataMessage(sismosJson: String) {
        try {
            val listType = object : TypeToken<List<Sismo>>() {}.type
            val sismos: List<Sismo> = gson.fromJson(sismosJson, listType)
            
            // Emitir sismos al ViewModel vía broadcast
            serviceScope.launch {
                SismosDataBroadcast.emitirSismos(sismos)
            }
            
            // Mostrar notificación visual para sismos críticos (magnitud >= 5.5)
            for (sismo in sismos) {
                if (sismo.magnitud >= 5.5) {
                    showNotification(
                        context = applicationContext,
                        title = "Earthquake M${sismo.magnitud}",
                        body = "${sismo.lugar} • Depth ${sismo.profundidadKm} km"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando sismos desde FCM data", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * Procesa notificaciones tradicionales de sismos críticos
     */
    private fun handleNotificationMessage(message: RemoteMessage) {
        val notification = message.notification ?: return
        val title = notification.title ?: "Earthquake Alert"
        val body = notification.body ?: "New earthquake detected"
        
        showNotification(
            context = applicationContext,
            title = title,
            body = body
        )
    }

    companion object {
        private const val TAG = "EarthquakeFCM"
    }
}

