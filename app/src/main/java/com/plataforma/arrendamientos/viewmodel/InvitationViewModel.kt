package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val invitations: StateFlow<List<Invitation>> = dataRepository.invitations

    fun getInvitationsByOwner(duenoId: String) =
        dataRepository.getInvitationsByOwner(duenoId)

    fun getInvitationByToken(token: String) =
        dataRepository.getInvitationByToken(token)

    fun createInvitation(
        propiedadId: String,
        duenoId: String,
        inquilinoCorreo: String,
        montoAlquiler: Double,
        montoDeposito: Double,
        moneda: Currency,
        notas: String = ""
    ): Invitation {
        val token = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val expiration = now + (7 * 24 * 60 * 60 * 1000L) // 7 days

        val invitation = Invitation(
            id = "inv-${System.currentTimeMillis()}",
            token = token,
            propiedadId = propiedadId,
            duenoId = duenoId,
            inquilinoCorreo = inquilinoCorreo,
            estado = InvitationStatus.PENDIENTE,
            fechaEmision = now.toString(),
            fechaExpiracion = expiration.toString(),
            montoAlquiler = montoAlquiler,
            montoDeposito = montoDeposito,
            moneda = moneda,
            notas = notas
        )

        dataRepository.addInvitation(invitation)
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
        return invitation
    }

    fun acceptInvitation(token: String, inquilinoId: String): Contract? {
        val invitation = dataRepository.getInvitationByToken(token) ?: return null
        if (invitation.estado != InvitationStatus.PENDIENTE) return null

        dataRepository.updateInvitation(
            invitation.copy(estado = InvitationStatus.ACEPTADA, inquilinoId = inquilinoId)
        )

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
        dataRepository.addContract(contract)

        dataRepository.addNotification(
            AppNotification(
                id = "notif-${System.currentTimeMillis()}",
                userId = invitation.duenoId,
                tipo = NotificationType.INVITACION_ACEPTADA,
                titulo = "Invitación aceptada",
                mensaje = "El inquilino ha aceptado la invitación y se ha creado el contrato.",
                leida = false,
                fecha = System.currentTimeMillis().toString()
            )
        )

        return contract
    }

    fun cancelInvitation(id: String) = dataRepository.cancelInvitation(id)
}
