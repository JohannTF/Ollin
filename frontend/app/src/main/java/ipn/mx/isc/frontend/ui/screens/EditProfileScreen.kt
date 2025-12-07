package ipn.mx.isc.frontend.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ipn.mx.isc.frontend.data.model.Usuario
import ipn.mx.isc.frontend.utils.ImageHelper
import ipn.mx.isc.frontend.utils.PermissionsHelper
import ipn.mx.isc.frontend.utils.rememberBase64Painter
import ipn.mx.isc.frontend.utils.rememberImagePermissionLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: Usuario,
    onSaveClick: (String, String?) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf(user.nombre) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var imagenBase64 by remember { mutableStateOf(user.imagenPerfilUrl) }
    var hasChanges by remember { mutableStateOf(false) }
    var isProcessingImage by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf<String?>(null) }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessingImage = true
            imageError = null
            imagenUri = it

            // Convertir a Base64 en un hilo secundario
            coroutineScope.launch {
                try {
                    val base64 = withContext(Dispatchers.IO) {
                        ImageHelper.uriToBase64(context, it, maxSizeKB = 500)
                    }

                    if (base64 != null) {
                        val isValid = ImageHelper.isValidBase64Image(base64)

                        if (isValid) {
                            imagenBase64 = base64
                            val sizeKB = ImageHelper.getBase64SizeKB(base64)
                            if (sizeKB > 1000) {
                                imageError = "Advertencia: Imagen grande (${sizeKB}KB). Considera usar una más pequeña."
                            } else {
                                imageError = null
                            }
                        } else {
                            imageError = "Error: Formato de imagen no válido"
                            imagenUri = null
                            imagenBase64 = null
                        }
                    } else {
                        imageError = "Error al procesar la imagen. Intenta con otra."
                        imagenUri = null
                        imagenBase64 = null
                    }
                } catch (e: Exception) {
                    Log.e("EditProfile", "Error procesando imagen", e)
                    imageError = "Error: ${e.message}"
                    imagenUri = null
                    imagenBase64 = null
                } finally {
                    isProcessingImage = false
                }
            }
        }
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberImagePermissionLauncher { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            imageError = "Se necesita permiso para acceder a las imágenes"
        }
    }

    // Función para abrir el selector de imágenes
    fun openImagePicker() {
        if (PermissionsHelper.hasImagePermission(context)) {
            imagePickerLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(PermissionsHelper.getImagePermission())
        }
    }

    // Detectar cambios
    LaunchedEffect(nombre, imagenBase64) {
        hasChanges = nombre != user.nombre || imagenBase64 != user.imagenPerfilUrl
    }

    // Usar el loader personalizado para Base64
    val base64Painter = rememberBase64Painter(imagenBase64)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onSaveClick(nombre, imagenBase64)
                        },
                        enabled = hasChanges && !isLoading && !isProcessingImage && nombre.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Foto de perfil
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (base64Painter != null) {
                    Image(
                        painter = base64Painter,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                if (!isProcessingImage && !isLoading) {
                                    openImagePicker()
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Mostrar avatar con inicial
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                if (!isProcessingImage && !isLoading) {
                                    openImagePicker()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = nombre.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Botón de cámara/editar
                if (!isProcessingImage) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                    ) {
                        FloatingActionButton(
                            onClick = { openImagePicker() },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = if (isProcessingImage) {
                    "Procesando imagen..."
                } else if (base64Painter == null && !imagenBase64.isNullOrEmpty()) {
                    "Cargando imagen..."
                } else {
                    "Toca la imagen para cambiarla"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isProcessingImage || (base64Painter == null && !imagenBase64.isNullOrEmpty()) ->
                        MaterialTheme.colorScheme.primary
                    else ->
                        MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // Mostrar error de imagen si existe
            imageError?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (error.startsWith("Advertencia"))
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (error.startsWith("Advertencia"))
                                Icons.Default.Info
                            else
                                Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (error.startsWith("Advertencia"))
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (error.startsWith("Advertencia"))
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { imageError = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = if (error.startsWith("Advertencia"))
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email (solo lectura)
            OutlinedTextField(
                value = user.correo,
                onValueChange = {},
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isProcessingImage
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para eliminar imagen
            if (imagenBase64 != null) {
                OutlinedButton(
                    onClick = {
                        imagenUri = null
                        imagenBase64 = null
                        imageError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && !isProcessingImage,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar foto de perfil")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Información adicional
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "La imagen se comprimirá automáticamente para optimizar el almacenamiento. El correo electrónico no puede ser modificado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}