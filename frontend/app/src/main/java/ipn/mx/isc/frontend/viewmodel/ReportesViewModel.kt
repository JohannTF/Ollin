package ipn.mx.isc.frontend.viewmodel

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipn.mx.isc.frontend.data.api.RetrofitClient
import ipn.mx.isc.frontend.data.model.ReporteSismico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class ReportesViewModel : ViewModel() {
    
    private val TAG = "ReportesViewModel"
    private val apiService = RetrofitClient.reportesApiService
    
    private val _reporteTrimestral = MutableStateFlow<UiState<ReporteSismico>>(UiState.Idle)
    val reporteTrimestral: StateFlow<UiState<ReporteSismico>> = _reporteTrimestral.asStateFlow()
    
    private val _reporteSemestral = MutableStateFlow<UiState<ReporteSismico>>(UiState.Idle)
    val reporteSemestral: StateFlow<UiState<ReporteSismico>> = _reporteSemestral.asStateFlow()
    
    private val _reporteAnual = MutableStateFlow<UiState<ReporteSismico>>(UiState.Idle)
    val reporteAnual: StateFlow<UiState<ReporteSismico>> = _reporteAnual.asStateFlow()
    
    private val _descargaEstado = MutableStateFlow<DescargaEstado>(DescargaEstado.Idle)
    val descargaEstado: StateFlow<DescargaEstado> = _descargaEstado.asStateFlow()
    
    /**
     * Carga el reporte trimestral (últimos 3 meses)
     */
    fun cargarReporteTrimestral() {
        viewModelScope.launch {
            _reporteTrimestral.value = UiState.Loading
            
            try {
                Log.d(TAG, "Cargando reporte trimestral...")
                val response = apiService.obtenerReporteTrimestral()
                
                if (response.isSuccessful && response.body() != null) {
                    _reporteTrimestral.value = UiState.Success(response.body()!!)
                    Log.d(TAG, "Reporte trimestral cargado: ${response.body()!!.totalSismos} sismos")
                } else {
                    val errorMsg = "Error al cargar reporte: ${response.code()}"
                    _reporteTrimestral.value = UiState.Error(errorMsg)
                    Log.e(TAG, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _reporteTrimestral.value = UiState.Error(errorMsg)
                Log.e(TAG, "Error cargando reporte trimestral", e)
            }
        }
    }
    
    /**
     * Carga el reporte semestral (últimos 6 meses)
     */
    fun cargarReporteSemestral() {
        viewModelScope.launch {
            _reporteSemestral.value = UiState.Loading
            
            try {
                Log.d(TAG, "Cargando reporte semestral...")
                val response = apiService.obtenerReporteSemestral()
                
                if (response.isSuccessful && response.body() != null) {
                    _reporteSemestral.value = UiState.Success(response.body()!!)
                    Log.d(TAG, "Reporte semestral cargado: ${response.body()!!.totalSismos} sismos")
                } else {
                    val errorMsg = "Error al cargar reporte: ${response.code()}"
                    _reporteSemestral.value = UiState.Error(errorMsg)
                    Log.e(TAG, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _reporteSemestral.value = UiState.Error(errorMsg)
                Log.e(TAG, "Error cargando reporte semestral", e)
            }
        }
    }
    
    /**
     * Carga el reporte anual (último año)
     */
    fun cargarReporteAnual() {
        viewModelScope.launch {
            _reporteAnual.value = UiState.Loading
            
            try {
                Log.d(TAG, "Cargando reporte anual...")
                val response = apiService.obtenerReporteAnual()
                
                if (response.isSuccessful && response.body() != null) {
                    _reporteAnual.value = UiState.Success(response.body()!!)
                    Log.d(TAG, "Reporte anual cargado: ${response.body()!!.totalSismos} sismos")
                } else {
                    val errorMsg = "Error al cargar reporte: ${response.code()}"
                    _reporteAnual.value = UiState.Error(errorMsg)
                    Log.e(TAG, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _reporteAnual.value = UiState.Error(errorMsg)
                Log.e(TAG, "Error cargando reporte anual", e)
            }
        }
    }
    
    // ========== FUNCIONES DE DESCARGA PDF ⭐ ==========
    
    /**
     * Descarga el reporte en formato PDF
     */
    fun descargarReportePdf(context: Context, tipo: TipoReporte) {
        viewModelScope.launch {
            _descargaEstado.value = DescargaEstado.Descargando
            
            try {
                Log.d(TAG, "Descargando PDF $tipo...")
                
                // Llamar al endpoint correspondiente
                val response = when (tipo) {
                    TipoReporte.TRIMESTRAL -> apiService.descargarReporteTrimestralPdf()
                    TipoReporte.SEMESTRAL -> apiService.descargarReporteSemestralPdf()
                    TipoReporte.ANUAL -> apiService.descargarReporteAnualPdf()
                }
                
                if (response.isSuccessful && response.body() != null) {
                    // Guardar el archivo
                    val fileName = generarNombreArchivo(tipo)
                    val file = guardarPdf(context, response.body()!!.bytes(), fileName)
                    
                    _descargaEstado.value = DescargaEstado.Exitoso(file.absolutePath)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "PDF descargado: ${file.name}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    
                    Log.d(TAG, "PDF descargado exitosamente: ${file.absolutePath}")
                } else {
                    val errorMsg = "Error al descargar PDF: ${response.code()}"
                    _descargaEstado.value = DescargaEstado.Error(errorMsg)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                    
                    Log.e(TAG, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _descargaEstado.value = DescargaEstado.Error(errorMsg)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
                
                Log.e(TAG, "Error descargando PDF", e)
            }
        }
    }
    
    /**
     * Guarda el PDF en el almacenamiento del dispositivo
     */
    private fun guardarPdf(context: Context, bytes: ByteArray, fileName: String): File {
        // Crear directorio si no existe
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "ReportesSismicos"
        )
        
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        // Crear archivo
        val file = File(directory, fileName)
        
        // Escribir bytes
        FileOutputStream(file).use { output ->
            output.write(bytes)
        }
        
        return file
    }
    
    /**
     * Genera el nombre del archivo PDF
     */
    private fun generarNombreArchivo(tipo: TipoReporte): String {
        val fecha = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val tipoStr = when (tipo) {
            TipoReporte.TRIMESTRAL -> "trimestral"
            TipoReporte.SEMESTRAL -> "semestral"
            TipoReporte.ANUAL -> "anual"
        }
        return "reporte_sismico_${tipoStr}_$fecha.pdf"
    }
    
    /**
     * Resetea el estado de descarga
     */
    fun resetearDescarga() {
        _descargaEstado.value = DescargaEstado.Idle
    }
}

// Estados de descarga
sealed class DescargaEstado {
    object Idle : DescargaEstado()
    object Descargando : DescargaEstado()
    data class Exitoso(val rutaArchivo: String) : DescargaEstado()
    data class Error(val mensaje: String) : DescargaEstado()
}

// Tipos de reporte
enum class TipoReporte {
    TRIMESTRAL,
    SEMESTRAL,
    ANUAL
}