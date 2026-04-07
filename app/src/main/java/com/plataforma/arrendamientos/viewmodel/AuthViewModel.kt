package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plataforma.arrendamientos.data.model.AuthState
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.data.model.UserRole
import com.plataforma.arrendamientos.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: StateFlow<User?> get() = _authState.let {
        MutableStateFlow(_authState.value.user)
    }

    init {
        // Observe persisted session
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.update { it.copy(user = user) }
            }
        }
    }

    fun login(correo: String, contrasena: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(correo, contrasena)
            result.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(user = user, isLoading = false) }
                    onSuccess()
                },
                onFailure = { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun register(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: UserRole,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(nombre, correo, contrasena, rol)
            result.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(user = user, isLoading = false) }
                    onSuccess()
                },
                onFailure = { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _authState.update { AuthState() }
            onComplete()
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }
}
