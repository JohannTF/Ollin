package ipn.mx.isc.frontend.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ipn.mx.isc.frontend.R

object NotificationUtils {
    private const val CHANNEL_ID = "earthquake_alerts"
    private const val CHANNEL_NAME = "Earthquake Alerts"
    private const val CHANNEL_DESC = "Notifications for significant earthquakes"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
