package ipn.mx.isc.frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipn.mx.isc.frontend.data.api.RetrofitClient
import ipn.mx.isc.frontend.data.model.Sismo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    
    private val _sismos = MutableStateFlow<List<Sismo>>(emptyList())
    val sismos: StateFlow<List<Sismo>> = _sismos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        cargarSismos()
    }
    
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
                _error.value = "Error al cargar sismos: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun cargarSismosRecientes(horas: Int = 24) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val resultado = RetrofitClient.sismosApiService.obtenerSismosRecientes(horas)
                _sismos.value = resultado
            } catch (e: Exception) {
                _error.value = "Error al cargar sismos recientes: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
