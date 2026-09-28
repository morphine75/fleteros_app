package com.logex.fleteros.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.logex.fleteros.data.AuthRepository
import com.logex.fleteros.data.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUsuarioChange(valor: String) {
        _uiState.value = _uiState.value.copy(usuario = valor, error = null)
    }

    fun onClaveChange(valor: String) {
        _uiState.value = _uiState.value.copy(clave = valor, error = null)
    }

    fun login() {
        val estadoActual = _uiState.value
        if (estadoActual.usuario.isBlank() || estadoActual.clave.isBlank()) {
            _uiState.value = estadoActual.copy(error = "Completa usuario y clave")
            return
        }

        _uiState.value = estadoActual.copy(cargando = true, error = null)

        viewModelScope.launch {
            when (val resultado = repository.login(estadoActual.usuario.trim(), estadoActual.clave)) {
                is LoginResult.Exito -> {
                    _uiState.value = _uiState.value.copy(cargando = false, loginExitoso = true)
                }
                is LoginResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }
}