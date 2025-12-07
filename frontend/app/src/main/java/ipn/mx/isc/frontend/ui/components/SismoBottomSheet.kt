package ipn.mx.isc.frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipn.mx.isc.frontend.data.model.Sismo
import ipn.mx.isc.frontend.utils.SismoColorUtils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SismoBottomSheet(
    sismo: Sismo,
    onDismiss: () -> Unit
) {
    val magnitudColor = SismoColorUtils.getColorForMagnitud(sismo.magnitud)
    val categoria = SismoColorUtils.getCategoriaDescripcion(sismo.magnitud)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header: Título (Lugar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sismo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sismo.lugar,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Magnitud Badge
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", sismo.magnitud),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = magnitudColor
                    )
                    Text(
                        text = "Mag",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Categoría
            Surface(
                color = magnitudColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = categoria,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = magnitudColor,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información del Evento
            Text(
                text = "Información del evento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Epicentro
            InfoRow(
                icon = Icons.Default.Place,
                label = "Epicentro",
                value = sismo.lugar,
                iconTint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Coordenadas (lat/lng)
            InfoRow(
                icon = Icons.Default.GpsFixed,
                label = "Coordenadas",
                value = String.format(Locale.US, "%.4f°, %.4f°", sismo.latitud, sismo.longitud),
                iconTint = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profundidad
            InfoRow(
                icon = Icons.Default.Layers,
                label = "Profundidad",
                value = "${String.format(Locale.US, "%.0f", sismo.profundidadKm)} km",
                iconTint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fecha y Hora (convertir de UTC a hora local de México)
            val fechaFormateada = try {
                val dateTimeUTC = OffsetDateTime.parse(sismo.fechaHora)
                val zonaHorariaMexico = java.time.ZoneId.of("America/Mexico_City")
                val dateTimeMexico = dateTimeUTC.atZoneSameInstant(zonaHorariaMexico)
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.US)
                "${dateTimeMexico.format(formatter)} (Hora de México)"
            } catch (e: Exception) {
                sismo.fechaHora
            }
            
            InfoRow(
                icon = Icons.Default.AccessTime,
                label = "Fecha y Hora",
                value = fechaFormateada,
                iconTint = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
