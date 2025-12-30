package ipn.mx.isc.frontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipn.mx.isc.frontend.data.api.RetrofitClient
import ipn.mx.isc.frontend.data.model.EstadoMexicano
import ipn.mx.isc.frontend.data.model.Sismo
import ipn.mx.isc.frontend.data.model.SismoFilter
import ipn.mx.isc.frontend.notification.SismosDataBroadcast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MapViewModel : ViewModel() {
    
    private val TAG = "MapViewModel"
    
    private val _sismos = MutableStateFlow<List<Sismo>>(emptyList())
    val sismos: StateFlow<List<Sismo>> = _sismos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _filtroActual = MutableStateFlow(SismoFilter.vacio())
    val filtroActual: StateFlow<SismoFilter> = _filtroActual.asStateFlow()
    
    private val _filtrosActivos = MutableStateFlow(false)
    val filtrosActivos: StateFlow<Boolean> = _filtrosActivos.asStateFlow()
    
    // Catálogo de estados cargado localmente
    private val _estados = MutableStateFlow(EstadoMexicano.obtenerTodos())
    val estados: StateFlow<List<EstadoMexicano>> = _estados.asStateFlow()
    
    init {
        // Aplicar filtro por defecto: sismos del mes actual
        aplicarFiltroMesActual()
        
        // Suscribirse a actualizaciones FCM data
        suscribirseAFcmData()
    }
    
    /**
     * Aplica filtro para mostrar solo sismos del mes actual
     */
    private fun aplicarFiltroMesActual() {
        viewModelScope.launch {
            val zonaHorariaMexico = ZoneId.of("America/Mexico_City")
            val primerDiaMes = LocalDate.now(zonaHorariaMexico).withDayOfMonth(1)
            val fechaActual = LocalDate.now(zonaHorariaMexico)
            
            // Convertir a UTC para enviar al backend
            val fechaInicioUTC = LocalDateTime.of(primerDiaMes, java.time.LocalTime.MIN)
                .atZone(zonaHorariaMexico)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            
            val fechaFinUTC = LocalDateTime.of(fechaActual, java.time.LocalTime.MAX)
                .atZone(zonaHorariaMexico)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            
            val filtroMesActual = SismoFilter(
                fechaInicio = fechaInicioUTC,
                fechaFin = fechaFinUTC
            )
            
            _filtroActual.value = filtroMesActual
            _filtrosActivos.value = true
            
            // Aplicar el filtro
            aplicarFiltros(filtroMesActual)
        }
    }
    
    /**
     * Carga inicial: obtiene datos existentes del servidor
     * Esto maneja el caso de cliente nuevo o que vuelve a abrir la app
     */
    fun cargarSismos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val resultado = RetrofitClient.sismosApiService.obtenerSismos(
                    page = 0,
                    size = 100
                )
                _sismos.value = resultado
            } catch (e: Exception) {
                _error.value = "Error al cargar sismos iniciales: ${e.message}"
                Log.e(TAG, "Error cargando datos iniciales", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Suscribirse a actualizaciones de sismos vía FCM data messaging
     */
    private fun suscribirseAFcmData() {
        viewModelScope.launch {
            SismosDataBroadcast.sismosFlow.collect { nuevosSismos ->
                // Fusionar nuevos sismos con los existentes
                val sismosActualizados = fusionarSismos(nuevosSismos, _sismos.value)
                _sismos.value = sismosActualizados
            }
        }
    }
    
    /**
     * Fusiona sismos nuevos con existentes, evitando duplicados
     * Los sismos se identifican por su ID único
     */
    private fun fusionarSismos(nuevos: List<Sismo>, existentes: List<Sismo>): List<Sismo> {
        val mapa = existentes.associateBy { it.id }.toMutableMap()
        
        // Agregar nuevos sismos (sobrescribe si ya existe el ID)
        nuevos.forEach { sismo ->
            mapa[sismo.id] = sismo
        }
        
        // Ordenar por fecha descendente (más recientes primero)
        return mapa.values
            .sortedByDescending { it.fechaHora }
            .take(100) // Mantener solo los 100 más recientes
    }
    
    /**
     * Aplicar filtros de búsqueda
     */
    fun aplicarFiltros(filtros: SismoFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _filtroActual.value = filtros
            _filtrosActivos.value = filtros.tieneFiltrosActivos()
            
            try {
                val resultado = RetrofitClient.sismosApiService.filtrarSismos(filtros)
                _sismos.value = resultado
            } catch (e: Exception) {
                _error.value = "Error al filtrar sismos: ${e.message}"
                Log.e(TAG, "Error aplicando filtros", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpiar filtros y volver a la vista por defecto
     */
    fun limpiarFiltros() {
        _filtroActual.value = SismoFilter.vacio()
        _filtrosActivos.value = false
        cargarSismos()
    }
}