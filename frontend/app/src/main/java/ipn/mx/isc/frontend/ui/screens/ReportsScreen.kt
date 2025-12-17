package ipn.mx.isc.frontend.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ipn.mx.isc.frontend.data.model.ReporteSismico
import ipn.mx.isc.frontend.viewmodel.*
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportesViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("3 Meses", "6 Meses", "1 Año")
    
    val descargaEstado by viewModel.descargaEstado.collectAsState()
    
    // Launcher para permisos de escritura (Android < 10)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, intentar descargar nuevamente
        }
    }
    
    // Manejar estado de descarga
    LaunchedEffect(descargaEstado) {
        when (val estado = descargaEstado) {
            is DescargaEstado.Exitoso -> {
                // Opcional: abrir el PDF automáticamente
                // abrirPdf(context, estado.rutaArchivo)
            }
            else -> {}
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header con botón de descarga
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Botón para descargar PDF del tab actual
                FilledTonalButton(
                    onClick = {
                        // Verificar permisos en Android < 10
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        
                        val tipo = when (selectedTab) {
                            0 -> TipoReporte.TRIMESTRAL
                            1 -> TipoReporte.SEMESTRAL
                            else -> TipoReporte.ANUAL
                        }
                        viewModel.descargarReportePdf(context, tipo)
                    },
                    enabled = descargaEstado !is DescargaEstado.Descargando
                ) {
                    if (descargaEstado is DescargaEstado.Descargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (descargaEstado is DescargaEstado.Descargando) "Descargando..."
                        else "Descargar PDF"
                    )
                }
            }
        }
        
        // Tabs para seleccionar periodo
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Contenido según tab seleccionado
        when (selectedTab) {
            0 -> ReporteContent(
                uiState = viewModel.reporteTrimestral.collectAsState().value,
                onLoad = { viewModel.cargarReporteTrimestral() },
                titulo = "Reporte Trimestral"
            )
            1 -> ReporteContent(
                uiState = viewModel.reporteSemestral.collectAsState().value,
                onLoad = { viewModel.cargarReporteSemestral() },
                titulo = "Reporte Semestral"
            )
            2 -> ReporteContent(
                uiState = viewModel.reporteAnual.collectAsState().value,
                onLoad = { viewModel.cargarReporteAnual() },
                titulo = "Reporte Anual"
            )
        }
    }
}

@Composable
private fun ReporteContent(
    uiState: UiState<ReporteSismico>,
    onLoad: () -> Unit,
    titulo: String
) {
    LaunchedEffect(Unit) {
        if (uiState is UiState.Idle) {
            onLoad()
        }
    }
    
    when (uiState) {
        is UiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generando reporte...")
                }
            }
        }
        
        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error al cargar reporte",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLoad) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }
            }
        }
        
        is UiState.Success -> {
            ReporteDetalle(reporte = uiState.data, titulo = titulo)
        }
    }
}

@Composable
private fun ReporteDetalle(
    reporte: ReporteSismico,
    titulo: String
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Generado: ${formatearFecha(reporte.fechaGeneracion)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cards de estadísticas principales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Sismos",
                value = formatearNumero(reporte.totalSismos),
                icon = Icons.Default.Home,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                )
            )
            
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Mag. Promedio",
                value = String.format("%.2f", reporte.magnitudPromedio),
                icon = Icons.Default.ThumbUp,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
                )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Mag. Máxima",
                value = String.format("%.1f", reporte.magnitudMaxima),
                icon = Icons.Default.Star,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFfa709a), Color(0xFFfee140))
                )
            )
            
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Prof. Promedio",
                value = "${String.format("%.1f", reporte.profundidadPromedio)} km",
                icon = Icons.Default.Place,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF30cfd0), Color(0xFF330867))
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Distribución por magnitud
        SectionTitle("Distribución por Magnitud")
        Spacer(modifier = Modifier.height(8.dp))
        DistribucionChart(
            data = reporte.distribucionPorMagnitud,
            color = Color(0xFF667eea)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top estados
        SectionTitle("Estados con Mayor Actividad")
        Spacer(modifier = Modifier.height(8.dp))
        DistribucionChart(
            data = reporte.distribucionPorEstado.toList().take(10).toMap(),
            color = Color(0xFFf5576c)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top 10 sismos más fuertes
        SectionTitle("Top 10 Sismos Más Fuertes")
        Spacer(modifier = Modifier.height(8.dp))
        reporte.sismosMasFuertes.forEachIndexed { index, sismo ->
            SismoCard(
                index = index + 1,
                sismo = sismo
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Función para abrir el PDF (opcional)
private fun abrirPdf(context: android.content.Context, rutaArchivo: String) {
    try {
        val file = File(rutaArchivo)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ... [Resto de funciones composables anteriores: StatCard, SectionTitle, etc.]

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DistribucionChart(
    data: Map<String, Long>,
    color: Color
) {
    val maxValue = data.values.maxOrNull()?.toFloat() ?: 1f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        data.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(100.dp),
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((value / maxValue).coerceIn(0.05f, 1f))
                            .background(color, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = formatearNumero(value),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SismoCard(
    index: Int,
    sismo: ipn.mx.isc.frontend.data.model.SismoResumen
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${sismo.fecha} ${sismo.hora}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sismo.ubicacion,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Profundidad: ${sismo.profundidad} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.1f", sismo.magnitud),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = obtenerColorMagnitud(sismo.magnitud)
                )
                Text(
                    text = "Magnitud",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun obtenerColorMagnitud(magnitud: Double): Color {
    return when {
        magnitud >= 7.0 -> Color(0xFFE53935)
        magnitud >= 6.0 -> Color(0xFFFF6F00)
        magnitud >= 5.0 -> Color(0xFFFFA000)
        magnitud >= 4.0 -> Color(0xFFFDD835)
        else -> Color(0xFF43A047)
    }
}

private fun formatearFecha(fecha: String): String {
    return fecha.replace("T", " ").substring(0, 16)
}

private fun formatearNumero(numero: Long): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(numero)
}