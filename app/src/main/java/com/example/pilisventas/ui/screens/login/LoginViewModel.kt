package com.example.pilisventas.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilisventas.data.model.Usuario
import com.example.pilisventas.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val checkingSession: Boolean = true,
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val usuario = authRepository.getCurrentUsuario()
            _uiState.update { it.copy(checkingSession = false, usuario = usuario) }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password).fold(
                onSuccess = { usuario ->
                    _uiState.update { it.copy(isLoading = false, usuario = usuario) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = "Correo o contraseña incorrectos") }
                }
            )
        }
    }
}
