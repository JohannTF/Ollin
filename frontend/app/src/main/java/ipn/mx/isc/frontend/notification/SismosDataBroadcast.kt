package ipn.mx.isc.frontend.notification

import ipn.mx.isc.frontend.data.model.Sismo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Broadcaster para emitir sismos recibidos por FCM a la UI.
 * Permite que EarthquakeMessagingService envíe sismos al ViewModel
 * sin mantener referencias directas.
 */
object SismosDataBroadcast {
    
    private val _sismosFlow = MutableSharedFlow<List<Sismo>>(replay = 0)
    val sismosFlow: SharedFlow<List<Sismo>> = _sismosFlow.asSharedFlow()

    suspend fun emitirSismos(sismos: List<Sismo>) {
        _sismosFlow.emit(sismos)
    }
}
