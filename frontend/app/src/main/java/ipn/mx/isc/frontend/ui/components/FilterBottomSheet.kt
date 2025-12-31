package ipn.mx.isc.frontend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ipn.mx.isc.frontend.data.model.EstadoMexicano
import ipn.mx.isc.frontend.data.model.SismoFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    onApply: (SismoFilter) -> Unit,
    onClear: () -> Unit,
    currentFilter: SismoFilter = SismoFilter.vacio(),
    estados: List<EstadoMexicano> = emptyList()
) {
    // Zona horaria de México (Ciudad de México)
    val zonaHorariaMexico = ZoneId.of("America/Mexico_City")
    
    // Valores por defecto: primer día del mes actual a las 00:00 hasta ahora
    val primerDiaMes = LocalDate.now(zonaHorariaMexico).withDayOfMonth(1)
    val fechaActual = LocalDate.now(zonaHorariaMexico)
    
    var magnitudMin by remember { mutableStateOf(currentFilter.magnitudMin?.toString() ?: "") }
    var magnitudMax by remember { mutableStateOf(currentFilter.magnitudMax?.toString() ?: "") }
    var fechaInicio by remember { mutableStateOf(currentFilter.fechaInicio?.let { 
        // Convertir de UTC a hora local de México para mostrar
        ZonedDateTime.parse(it).withZoneSameInstant(zonaHorariaMexico).toLocalDate()
    } ?: primerDiaMes) }
    var fechaFin by remember { mutableStateOf(currentFilter.fechaFin?.let {
        ZonedDateTime.parse(it).withZoneSameInstant(zonaHorariaMexico).toLocalDate()
    } ?: fechaActual) }
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    var estadoSeleccionado by remember { mutableStateOf(currentFilter.estado ?: "") }
    var expandedEstado by remember { mutableStateOf(false) }
    var profundidadMin by remember { mutableStateOf(currentFilter.profundidadMin?.toString() ?: "") }
    var profundidadMax by remember { mutableStateOf(currentFilter.profundidadMax?.toString() ?: "") }
    
    // Formateador para mostrar fechas en formato legible
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros de Sismos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, "Cerrar")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filtro por Magnitud
            Text(
                text = "Magnitud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = magnitudMin,
                    onValueChange = { magnitudMin = it },
                    label = { Text("Mínima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = magnitudMax,
                    onValueChange = { magnitudMax = it },
                    label = { Text("Máxima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filtro por Fecha
            Text(
                text = "Rango de Fechas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fecha inicio
                OutlinedTextField(
                    value = fechaInicio.format(dateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Desde") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerInicio = true }) {
                            Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                // Fecha fin
                OutlinedTextField(
                    value = fechaFin.format(dateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hasta") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerFin = true }) {
                            Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filtro por Estado
            Text(
                text = "Estado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedEstado,
                onExpandedChange = { expandedEstado = !expandedEstado }
            ) {
                OutlinedTextField(
                    value = estadoSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar estado") },
                    placeholder = { Text("Todos los estados") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expandedEstado,
                    onDismissRequest = { expandedEstado = false }
                ) {
                    // Opción para limpiar selección
                    DropdownMenuItem(
                        text = { Text("Todos los estados") },
                        onClick = {
                            estadoSeleccionado = ""
                            expandedEstado = false
                        }
                    )
                    HorizontalDivider()
                    
                    // Lista de estados
                    estados.forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.nombreCompleto) },
                            onClick = {
                                estadoSeleccionado = estado.nombreCompleto
                                expandedEstado = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filtro por Profundidad
            Text(
                text = "Profundidad (km)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = profundidadMin,
                    onValueChange = { profundidadMin = it },
                    label = { Text("Mínima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = profundidadMax,
                    onValueChange = { profundidadMax = it },
                    label = { Text("Máxima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        magnitudMin = ""
                        magnitudMax = ""
                        fechaInicio = primerDiaMes
                        fechaFin = fechaActual
                        estadoSeleccionado = ""
                        profundidadMin = ""
                        profundidadMax = ""
                        onClear()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
                
                Button(
                    onClick = {
                        // Convertir fechas locales a UTC para enviar al backend 
                        val fechaInicioUTC = LocalDateTime.of(fechaInicio, java.time.LocalTime.of(0, 0, 0))
                            .atZone(zonaHorariaMexico)
                            .withZoneSameInstant(ZoneId.of("UTC"))
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        
                        val fechaFinUTC = LocalDateTime.of(fechaFin, java.time.LocalTime.of(23, 59, 59))
                            .atZone(zonaHorariaMexico)
                            .withZoneSameInstant(ZoneId.of("UTC"))
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        
                        val filter = SismoFilter(
                            magnitudMin = magnitudMin.toDoubleOrNull(),
                            magnitudMax = magnitudMax.toDoubleOrNull(),
                            fechaInicio = fechaInicioUTC,
                            fechaFin = fechaFinUTC,
                            estado = estadoSeleccionado.takeIf { it.isNotBlank() },
                            profundidadMin = profundidadMin.toDoubleOrNull(),
                            profundidadMax = profundidadMax.toDoubleOrNull()
                        )
                        onApply(filter)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aplicar")
                }
            }
        }
    }
    
    // Date Picker para fecha de inicio
    if (showDatePickerInicio) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicio.atStartOfDay(zonaHorariaMexico).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaInicio = java.time.Instant.ofEpochMilli(millis)
                            .atZone(zonaHorariaMexico)
                            .toLocalDate()
                    }
                    showDatePickerInicio = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerInicio = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Date Picker para fecha fin
    if (showDatePickerFin) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaFin.atStartOfDay(zonaHorariaMexico).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaFin = java.time.Instant.ofEpochMilli(millis)
                            .atZone(zonaHorariaMexico)
                            .toLocalDate()
                    }
                    showDatePickerFin = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerFin = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
