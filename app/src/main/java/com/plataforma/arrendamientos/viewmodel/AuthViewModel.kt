package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.plataforma.arrendamientos.data.model.AuthState
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.data.model.UserRole
import com.plataforma.arrendamientos.data.repository.AuthRepository
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: StateFlow<User?> get() = _authState.let {
        MutableStateFlow(_authState.value.user)
    }

    init {
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
                    registrarTokenFcm(user.id)
                    onSuccess()
                },
                onFailure = { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun register(nombre: String, correo: String, contrasena: String, rol: UserRole, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(nombre, correo, contrasena, rol)
            result.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(user = user, isLoading = false) }
                    registrarTokenFcm(user.id)
                    onSuccess()
                },
                onFailure = { e ->
                    _authState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    private fun registrarTokenFcm(userId: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                dataRepository.registrarDispositivo(userId = userId, fcmToken = token)
            } catch (_: Exception) {
                // No bloqueamos el login si FCM falla
            }
        }
    }

    // ─── Google Sign-In ────────────────────────────────────────────────────────
    // isNewUser: true si el correo no existe aún, false si ya tiene cuenta.
    // onNeedsRole: se llama cuando es usuario nuevo y hay que preguntarle el rol.
    // onSuccess: se llama con el usuario listo en ambos casos.

    fun isExistingGoogleUser(correo: String): Boolean =
        authRepository.findUserByEmail(correo) != null

    fun loginOrRegisterWithGoogle(
        nombre: String,
        correo: String,
        googleId: String,
        rol: UserRole,
        onSuccess: (User) -> Unit
    ) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.loginOrRegisterWithGoogle(nombre, correo, googleId, rol)
            result.fold(
                onSuccess = { user ->
                    _authState.update { it.copy(user = user, isLoading = false) }
                    onSuccess(user)
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
