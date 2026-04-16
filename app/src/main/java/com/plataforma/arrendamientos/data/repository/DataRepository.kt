package com.plataforma.arrendamientos.data.repository

import com.plataforma.arrendamientos.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor() {

    // ─── Properties ────────────────────────────────────────────────────────────

    private val _properties = MutableStateFlow(MockData.MOCK_PROPERTIES.toMutableList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    fun getPropertiesByOwner(duenoId: String) =
        _properties.value.filter { it.duenoId == duenoId }

    fun getPropertyById(id: String) = _properties.value.find { it.id == id }

    fun addProperty(property: Property) {
        _properties.update { list ->
            (list + property).toMutableList()
        }
    }

    fun updateProperty(property: Property) {
        _properties.update { list ->
            list.map { if (it.id == property.id) property else it }.toMutableList()
        }
    }

    fun deleteProperty(id: String) {
        _properties.update { list ->
            list.filter { it.id != id }.toMutableList()
        }
    }

    // ─── Invitations ───────────────────────────────────────────────────────────

    private val _invitations = MutableStateFlow(mutableListOf<Invitation>())
    val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

    fun getInvitationsByOwner(duenoId: String) =
        _invitations.value.filter { it.duenoId == duenoId }

    fun getInvitationByToken(token: String) =
        _invitations.value.find { it.token == token }

    fun addInvitation(invitation: Invitation) {
        _invitations.update { list ->
            (list + invitation).toMutableList()
        }
    }

    fun updateInvitation(invitation: Invitation) {
        _invitations.update { list ->
            list.map { if (it.id == invitation.id) invitation else it }.toMutableList()
        }
    }

    fun cancelInvitation(id: String) {
        _invitations.update { list ->
            list.map {
                if (it.id == id) it.copy(estado = InvitationStatus.CANCELADA) else it
            }.toMutableList()
        }
    }

    // ─── Contracts ─────────────────────────────────────────────────────────────

    private val _contracts = MutableStateFlow(mutableListOf(MockData.MOCK_CONTRACT))
    val contracts: StateFlow<List<Contract>> = _contracts.asStateFlow()

    fun getContractByInquilino(inquilinoId: String) =
        _contracts.value.find { it.inquilinoId == inquilinoId && it.estado == ContractStatus.ACTIVO }

    fun getContractsByOwner(duenoId: String) =
        _contracts.value.filter { it.duenoId == duenoId }

    fun addContract(contract: Contract) {
        _contracts.update { list ->
            (list + contract).toMutableList()
        }
    }

    // ─── Payments ──────────────────────────────────────────────────────────────

    private val _payments = MutableStateFlow(MockData.MOCK_PAYMENTS.toMutableList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    fun getPaymentsByContract(contratoId: String) =
        _payments.value.filter { it.contratoId == contratoId }

    fun getPaymentsByOwner(duenoId: String) =
        _payments.value.filter { it.duenoId == duenoId }

    fun getPendingPaymentsByOwner(duenoId: String) =
        _payments.value.filter { it.duenoId == duenoId && it.estado == PaymentStatus.PENDIENTE }

    fun addPayment(payment: Payment) {
        _payments.update { list ->
            (list + payment).toMutableList()
        }
    }

    fun approvePayment(id: String) {
        val now = System.currentTimeMillis().toString()
        _payments.update { list ->
            list.map {
                if (it.id == id) it.copy(estado = PaymentStatus.APROBADO, fechaRevision = now)
                else it
            }.toMutableList()
        }
    }

    fun rejectPayment(id: String, motivo: String) {
        val now = System.currentTimeMillis().toString()
        _payments.update { list ->
            list.map {
                if (it.id == id) it.copy(
                    estado = PaymentStatus.RECHAZADO,
                    fechaRevision = now,
                    motivoRechazo = motivo
                ) else it
            }.toMutableList()
        }
    }

    // ─── Notifications ─────────────────────────────────────────────────────────

    private val _notifications = MutableStateFlow(mutableListOf<AppNotification>())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun getNotificationsByUser(userId: String) =
        _notifications.value.filter { it.userId == userId }

    fun addNotification(notification: AppNotification) {
        _notifications.update { list ->
            (listOf(notification) + list).toMutableList()
        }
    }

    fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(leida = true) else it }.toMutableList()
        }
    }

    fun markAllNotificationsRead(userId: String) {
        _notifications.update { list ->
            list.map {
                if (it.userId == userId) it.copy(leida = true) else it
            }.toMutableList()
        }
    }

    // ─── Messages ──────────────────────────────────────────────────────────────

    private val _conversations = MutableStateFlow(mutableListOf<Conversation>())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow(mutableListOf<Message>())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun getConversationsByUser(userId: String) =
        _conversations.value.filter { userId in it.participants }

    fun getMessagesByConversation(conversationId: String) =
        _messages.value.filter { it.conversationId == conversationId }

    fun sendMessage(message: Message) {
        _messages.update { list ->
            (list + message).toMutableList()
        }
        _conversations.update { list ->
            list.map {
                if (it.id == message.conversationId) it.copy(
                    lastMessage = message.content,
                    lastMessageAt = message.timestamp
                ) else it
            }.toMutableList()
        }
    }

    fun getOrCreateConversation(
        user1Id: String,
        user2Id: String,
        propertyId: String? = null
    ): Conversation {
        val existing = _conversations.value.find {
            user1Id in it.participants && user2Id in it.participants
        }
        if (existing != null) return existing

        val conv = Conversation(
            id = "conv-${System.currentTimeMillis()}",
            participants = listOf(user1Id, user2Id),
            propertyId = propertyId,
            createdAt = System.currentTimeMillis().toString()
        )
        _conversations.update { list -> (list + conv).toMutableList() }
        return conv
    }
}
