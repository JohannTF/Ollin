package ipn.mx.isc.frontend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ipn.mx.isc.frontend.data.model.Usuario
import ipn.mx.isc.frontend.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        checkSavedSession()
    }

    private fun checkSavedSession() {
        viewModelScope.launch {
            _isLoading.value = true

            val savedUser = repository.getSavedUser()
            if (savedUser != null) {
                _currentUser.value = savedUser
                _isAuthenticated.value = true
            }

            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            if (email.isBlank() || password.isBlank()) {
                _error.value = "Por favor completa todos los campos"
                _isLoading.value = false
                return@launch
            }

            if (!email.contains("@")) {
                _error.value = "Email inválido"
                _isLoading.value = false
                return@launch
            }

            val result = repository.login(email, password)

            result.onSuccess { usuario ->
                _currentUser.value = usuario
                _isAuthenticated.value = true
            }.onFailure { error ->
                _error.value = error.message ?: "Error al iniciar sesión"
            }

            _isLoading.value = false
        }
    }

    fun registro(nombre: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                _error.value = "Por favor completa todos los campos"
                _isLoading.value = false
                return@launch
            }

            if (!correo.contains("@")) {
                _error.value = "Email inválido"
                _isLoading.value = false
                return@launch
            }

            if (contrasena.length < 6) {
                _error.value = "La contraseña debe tener al menos 6 caracteres"
                _isLoading.value = false
                return@launch
            }

            val result = repository.registro(nombre, correo, contrasena)

            result.onSuccess { usuario ->
                _currentUser.value = usuario
                _isAuthenticated.value = true
                _successMessage.value = "Registro exitoso"
            }.onFailure { error ->
                _error.value = error.message ?: "Error al registrarse"
            }

            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            _currentUser.value = null
            _isAuthenticated.value = false
            _error.value = null
        }
    }

    fun updateProfile(nombre: String, imagenUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val usuarioId = _currentUser.value?.id
            if (usuarioId == null) {
                _error.value = "Usuario no autenticado"
                _isLoading.value = false
                return@launch
            }

            val result = repository.actualizarPerfil(usuarioId, nombre, imagenUrl)

            result.onSuccess { usuario ->
                _currentUser.value = usuario
                _successMessage.value = "Perfil actualizado exitosamente"
            }.onFailure { error ->
                _error.value = error.message ?: "Error al actualizar perfil"
            }

            _isLoading.value = false
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val usuarioId = _currentUser.value?.id
            if (usuarioId == null) {
                _error.value = "Usuario no autenticado"
                _isLoading.value = false
                return@launch
            }

            if (newPassword.length < 6) {
                _error.value = "La contraseña debe tener al menos 6 caracteres"
                _isLoading.value = false
                return@launch
            }

            val result = repository.cambiarPassword(usuarioId, currentPassword, newPassword)

            result.onSuccess { mensaje ->
                _successMessage.value = mensaje
            }.onFailure { error ->
                _error.value = error.message ?: "Error al cambiar contraseña"
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}