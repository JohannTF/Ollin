package ipn.mx.isc.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ipn.mx.isc.frontend.viewmodel.AuthViewModel

enum class SettingsScreenState {
    LOGIN,
    REGISTRO,
    PROFILE,
    EDIT_PROFILE,
    CHANGE_PASSWORD
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var screenState by remember { mutableStateOf(SettingsScreenState.LOGIN) }

    // Determinar el estado de la pantalla basado en la autenticación
    LaunchedEffect(currentUser) {
        if (currentUser != null &&
            (screenState == SettingsScreenState.LOGIN || screenState == SettingsScreenState.REGISTRO)) {
            screenState = SettingsScreenState.PROFILE
        } else if (currentUser == null &&
            screenState != SettingsScreenState.LOGIN &&
            screenState != SettingsScreenState.REGISTRO) {
            screenState = SettingsScreenState.LOGIN
        }
    }

    // Mostrar mensaje de éxito
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccessMessage()
            // Volver al perfil después de operaciones exitosas
            if (screenState == SettingsScreenState.EDIT_PROFILE ||
                screenState == SettingsScreenState.CHANGE_PASSWORD) {
                screenState = SettingsScreenState.PROFILE
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (screenState) {
            SettingsScreenState.LOGIN -> {
                if (isLoading && currentUser == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            viewModel.login(email, password)
                        },
                        onRegistroClick = {
                            viewModel.clearError()
                            screenState = SettingsScreenState.REGISTRO
                        },
                        isLoading = isLoading,
                        error = error,
                        onErrorDismiss = { viewModel.clearError() }
                    )
                }
            }

            SettingsScreenState.REGISTRO -> {
                RegistroScreen(
                    onRegistroClick = { nombre, email, password ->
                        viewModel.registro(nombre, email, password)
                    },
                    onBackToLoginClick = {
                        viewModel.clearError()
                        screenState = SettingsScreenState.LOGIN
                    },
                    isLoading = isLoading,
                    error = error,
                    onErrorDismiss = { viewModel.clearError() }
                )
            }

            SettingsScreenState.PROFILE -> {
                currentUser?.let { user ->
                    ProfileScreen(
                        user = user,
                        onEditProfileClick = {
                            screenState = SettingsScreenState.EDIT_PROFILE
                        },
                        onChangePasswordClick = {
                            screenState = SettingsScreenState.CHANGE_PASSWORD
                        },
                        onLogoutClick = {
                            viewModel.logout()
                            screenState = SettingsScreenState.LOGIN
                        }
                    )
                }
            }

            SettingsScreenState.EDIT_PROFILE -> {
                currentUser?.let { user ->
                    EditProfileScreen(
                        user = user,
                        onSaveClick = { nombre, imagenUrl ->
                            viewModel.updateProfile(nombre, imagenUrl)
                        },
                        onBackClick = {
                            viewModel.clearError()
                            screenState = SettingsScreenState.PROFILE
                        },
                        isLoading = isLoading
                    )
                }
            }

            SettingsScreenState.CHANGE_PASSWORD -> {
                ChangePasswordScreen(
                    onChangePasswordClick = { currentPassword, newPassword ->
                        viewModel.changePassword(currentPassword, newPassword)
                    },
                    onBackClick = {
                        viewModel.clearError()
                        viewModel.clearSuccessMessage()
                        screenState = SettingsScreenState.PROFILE
                    },
                    isLoading = isLoading,
                    error = error,
                    success = successMessage,
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
        }

        // Snackbar para mensajes de éxito (excepto en cambio de contraseña que ya tiene su propio snackbar)
        if (successMessage != null && screenState != SettingsScreenState.CHANGE_PASSWORD) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = successMessage!!,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}