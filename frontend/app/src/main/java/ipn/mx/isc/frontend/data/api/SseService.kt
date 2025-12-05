package ipn.mx.isc.frontend.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ipn.mx.isc.frontend.BuildConfig
import ipn.mx.isc.frontend.data.model.Sismo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class SseService {
    
    private val TAG = "SseService"
    private val gson = Gson()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(0, TimeUnit.MILLISECONDS) // Sin timeout para SSE
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    
    /**
     * Conecta al endpoint SSE y emite nuevos sismos en tiempo real
     */
    fun conectarSseStream(): Flow<List<Sismo>> = callbackFlow {
        val request = Request.Builder()
            .url("${BuildConfig.BACKEND_URL}api/sismos/stream")
            .header("Accept", "text/event-stream")
            .build()
        
        val eventSourceListener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE Conexión abierta")
            }
            
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                Log.d(TAG, "SSE Evento recibido - tipo: $type, data: ${data.take(100)}")
                
                // El backend envía eventos con nombre "nuevos-sismos"
                if (type == "nuevos-sismos") {
                    try {
                        val listType = object : TypeToken<List<Sismo>>() {}.type
                        val nuevosSismos: List<Sismo> = gson.fromJson(data, listType)
                        Log.d(TAG, "SSE Parseados ${nuevosSismos.size} sismos nuevos")
                        trySend(nuevosSismos)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando sismos desde SSE", e)
                    }
                }
            }
            
            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE Conexión cerrada")
                channel.close()
            }
            
            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e(TAG, "SSE Error de conexión: ${t?.message}", t)
                channel.close(t)
            }
        }
        
        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, eventSourceListener)
        
        awaitClose {
            Log.d(TAG, "SSE Cerrando conexión")
            eventSource.cancel()
        }
    }
}
