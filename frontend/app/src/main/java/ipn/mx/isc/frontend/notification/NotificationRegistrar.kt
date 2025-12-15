package ipn.mx.isc.frontend.notification

import android.util.Log
import ipn.mx.isc.frontend.data.api.RetrofitClient
import ipn.mx.isc.frontend.data.model.DeviceTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationRegistrar {

    private const val TAG = "NotificationRegistrar"

    fun registerToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.notificationApiService.registrarToken(
                    DeviceTokenRequest(token = token, platform = "android")
                )
                Log.d(TAG, "Token registered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error registering token", e)
            }
        }
    }
}
