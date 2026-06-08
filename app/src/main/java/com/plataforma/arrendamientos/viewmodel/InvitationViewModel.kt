package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val invitations: StateFlow<List<Invitation>> = dataRepository.invitations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun getInvitationsByOwner(duenoId: String) = dataRepository.getInvitationsByOwner(duenoId)
    fun getInvitationByToken(token: String) = dataRepository.getInvitationByToken(token)

    fun refreshInvitations(userId: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            dataRepository.refreshInvitations(userId)
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun createInvitation(
        propiedadId: String,
        duenoId: String,
        inquilinoCorreo: String,
        montoAlquiler: Double,
        montoDeposito: Double,
        moneda: Currency,
        notas: String = "",
        onSuccess: (token: String) -> Unit = {}
    ) {
        val token = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val invitation = Invitation(
            id = "inv-${now}",
            token = token,
            propiedadId = propiedadId,
            duenoId = duenoId,
            inquilinoCorreo = inquilinoCorreo,
            estado = InvitationStatus.PENDIENTE,
            fechaEmision = now.toString(),
            fechaExpiracion = (now + 7 * 24 * 60 * 60 * 1000L).toString(),
            montoAlquiler = montoAlquiler,
            montoDeposito = montoDeposito,
            moneda = moneda,
            notas = notas
        )
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.createInvitationApi(invitation)
                .onSuccess { created ->
                    dataRepository.addNotification(
                        AppNotification(
                            id = "notif-${System.currentTimeMillis()}",
                            userId = duenoId,
                            tipo = NotificationType.INVITACION_ENVIADA,
                            titulo = "Invitación enviada",
                            mensaje = "Se ha enviado una invitación a $inquilinoCorreo.",
                            leida = false,
                            fecha = System.currentTimeMillis().toString()
                        )
                    )
                    onSuccess(created.token.ifBlank { token })
                }
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun acceptInvitation(token: String, inquilinoId: String, onSuccess: () -> Unit = {}) {
        val invitation = dataRepository.getInvitationByToken(token) ?: run {
            _error.update { "Invitación no encontrada" }
            return
        }
        if (invitation.estado != InvitationStatus.PENDIENTE) {
            _error.update { "Esta invitación ya no está disponible" }
            return
        }
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.updateInvitationStatusApi(invitation.id, "aceptada")
                .onSuccess {
                    val contract = Contract(
                        id = "contract-${System.currentTimeMillis()}",
                        invitacionId = invitation.id,
                        propiedadId = invitation.propiedadId,
                        duenoId = invitation.duenoId,
                        inquilinoId = inquilinoId,
                        montoMensual = invitation.montoAlquiler,
                        montoDeposito = invitation.montoDeposito,
                        moneda = invitation.moneda,
                        fechaInicio = System.currentTimeMillis().toString(),
                        estado = ContractStatus.ACTIVO,
                        estadoDeposito = DepositStatus.PENDIENTE
                    )
                    dataRepository.createContractApi(contract)
                    dataRepository.addNotification(
                        AppNotification(
                            id = "notif-${System.currentTimeMillis()}",
                            userId = invitation.duenoId,
                            tipo = NotificationType.INVITACION_ACEPTADA,
                            titulo = "Invitación aceptada",
                            mensaje = "El inquilino ha aceptado la invitación.",
                            leida = false,
                            fecha = System.currentTimeMillis().toString()
                        )
                    )
                    onSuccess()
                }
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun cancelInvitation(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.updateInvitationStatusApi(id, "cancelada")
                .onSuccess { onSuccess() }
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun clearError() = _error.update { null }
}
