package ipn.mx.isc.frontend.notification

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

    private fun handleSismosDataMessage(sismosJson: String) {
        try {
            val listType = object : TypeToken<List<Sismo>>() {}.type
            val sismos: List<Sismo> = gson.fromJson(sismosJson, listType)
            
            // Emitir sismos al ViewModel vía broadcast
            serviceScope.launch {
                SismosDataBroadcast.emitirSismos(sismos)
            }
        } catch (e: Exception) {
            // Error ocurred
        }
    }

    private fun handleNotificationMessage(message: RemoteMessage) {
        val notification = message.notification ?: return
        val title = notification.title ?: "🚨 ALERTA! Sismo Detectado"
        val body = notification.body ?: "NUEVO SISMO!!"
        
        showNotification(
            context = applicationContext,
            title = title,
            body = body
        )
    }
}

