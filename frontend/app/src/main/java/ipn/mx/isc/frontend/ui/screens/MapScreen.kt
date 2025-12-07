package ipn.mx.isc.frontend.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import ipn.mx.isc.frontend.data.model.Sismo
import ipn.mx.isc.frontend.ui.components.FilterBottomSheet
import ipn.mx.isc.frontend.ui.components.PulsingCirclesAnimation
import ipn.mx.isc.frontend.ui.components.PulsingSismoMarker
import ipn.mx.isc.frontend.ui.components.SismoBottomSheet
import ipn.mx.isc.frontend.utils.SismoColorUtils
import ipn.mx.isc.frontend.viewmodel.MapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedSismo by remember { mutableStateOf<Sismo?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(19.4326, -99.1332), 6f)
    }
    
    // Centrar en el sismo seleccionado con offset
    LaunchedEffect(selectedSismo) {
        selectedSismo?.let { sismo ->
            val targetLat = sismo.latitud - 0.6
            val targetLatLng = LatLng(targetLat, sismo.longitud)
            
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(targetLatLng, 8f),
                durationMs = 1200
            )
        }
    }
    
    // Observar los sismos del ViewModel
    val sismos by viewModel.sismos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filtrosActivos by viewModel.filtrosActivos.collectAsState()
    val filtroActual by viewModel.filtroActual.collectAsState()
    val estados by viewModel.estados.collectAsState()
    
    // Recarga automática cada 3 minutos
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(180000)
            viewModel.cargarSismos()
        }
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userLocation = latLng
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                durationMs = 1000
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    if (visible) {
        Box(modifier = modifier.fillMaxSize()) {
            if (locationPermissionsState.allPermissionsGranted) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    // Marcador de ubicación del usuario
                    userLocation?.let { location ->
                        MarkerComposable(
                            state = MarkerState(location),
                            anchor = Offset(0.5f, 0.5f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(80.dp)
                            ) {
                                PulsingCirclesAnimation(
                                    color = Color(0xFF4285F4),
                                    baseSize = 12f
                                )
                            }
                        }
                    }
                    
                    // Marcadores de sismos
                    sismos.forEach { sismo ->
                        val color = SismoColorUtils.getColorForMagnitud(sismo.magnitud)
                        val baseSize = SismoColorUtils.getSizeForMagnitud(sismo.magnitud)
                        
                        PulsingSismoMarker(
                            position = com.google.android.gms.maps.model.LatLng(sismo.latitud, sismo.longitud),
                            color = color,
                            baseSize = baseSize,
                            isSelected = selectedSismo?.id == sismo.id,
                            onClick = {
                                selectedSismo = sismo
                                showBottomSheet = true
                            }
                        )
                    }
                }

                // Botón para abrir filtros
                FloatingActionButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 88.dp), // Espacio para el botón de ubicación
                    containerColor = if (filtrosActivos) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (filtrosActivos) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtrar sismos"
                    )
                }

                // Botón para centrar en ubicación del usuario
                FloatingActionButton(
                    onClick = {
                        userLocation?.let {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(it, 15f),
                                    durationMs = 1000
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centrar en mi ubicación"
                    )
                }
                
                // Progress bar (carga de sismos) 
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Mostrar error si existe
                error?.let { errorMsg ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.cargarSismos() }) {
                                Text("Reintentar")
                            }
                        }
                    ) {
                        Text(errorMsg)
                    }
                }
                
                // Bottom Sheet con detalles del sismo
                if (showBottomSheet && selectedSismo != null) {
                    SismoBottomSheet(
                        sismo = selectedSismo!!,
                        onDismiss = {
                            showBottomSheet = false
                            selectedSismo = null
                        }
                    )
                }
                
                // Bottom Sheet de filtros
                if (showFilterSheet) {
                    FilterBottomSheet(
                        onDismiss = { showFilterSheet = false },
                        onApply = { filtro ->
                            viewModel.aplicarFiltros(filtro)
                        },
                        onClear = {
                            viewModel.limpiarFiltros()
                        },
                        currentFilter = filtroActual,
                        estados = estados
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Se requieren permisos de ubicación",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                        Text("Conceder permisos")
                    }
                }
            }
        }
    }
}
