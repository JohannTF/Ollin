package ipn.mx.isc.frontend.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ipn.mx.isc.frontend.notification.NotificationUtils.showNotification

class EarthquakeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        NotificationRegistrar.registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val magnitude = data["magnitud"] ?: ""
        val lugar = data["lugar"] ?: ""
        val profundidad = data["profundidadKm"] ?: ""

        val title = data["title"] ?: "Earthquake Alert"
        val body = data["body"] ?: if (magnitude.isNotEmpty()) {
            "M$magnitude • $lugar • Depth ${profundidad}km"
        } else {
            lugar.ifEmpty { "New earthquake detected" }
        }

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
