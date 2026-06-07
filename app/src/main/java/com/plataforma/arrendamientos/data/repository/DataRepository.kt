package com.plataforma.arrendamientos.data.repository

import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.remote.ApiService
import com.plataforma.arrendamientos.data.remote.MsMensajesEnviarRequest
import com.plataforma.arrendamientos.data.remote.UpdateNotificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ─── Properties ────────────────────────────────────────────────────────────

    private val _properties = MutableStateFlow(mutableListOf<Property>())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    fun getPropertiesByOwner(duenoId: String) = _properties.value.filter { it.duenoId == duenoId }
    fun getPropertyById(id: String) = _properties.value.find { it.id == id }

    fun addProperty(property: Property) {
        _properties.update { list -> (list + property).toMutableList() }
    }

    fun updateProperty(property: Property) {
        _properties.update { list -> list.map { if (it.id == property.id) property else it }.toMutableList() }
    }

    fun deleteProperty(id: String) {
        _properties.update { list -> list.filter { it.id != id }.toMutableList() }
    }

    suspend fun refreshProperties(): Result<Unit> = runApiCall {
        val props = apiService.getProperties().bodyOrThrow().mapNotNull { it.toDomain() }
        _properties.value = props.toMutableList()
    }

    // ─── Invitations ───────────────────────────────────────────────────────────

    private val _invitations = MutableStateFlow(mutableListOf<Invitation>())
    val invitations: StateFlow<List<Invitation>> = _invitations.asStateFlow()

    fun getInvitationsByOwner(duenoId: String) = _invitations.value.filter { it.duenoId == duenoId }
    fun getInvitationByToken(token: String) = _invitations.value.find { it.token == token }

    fun addInvitation(invitation: Invitation) {
        _invitations.update { list -> (list + invitation).toMutableList() }
    }

    fun updateInvitation(invitation: Invitation) {
        _invitations.update { list -> list.map { if (it.id == invitation.id) invitation else it }.toMutableList() }
    }

    fun cancelInvitation(id: String) {
        _invitations.update { list ->
            list.map { if (it.id == id) it.copy(estado = InvitationStatus.CANCELADA) else it }.toMutableList()
        }
    }

    suspend fun refreshInvitations(): Result<Unit> = runApiCall {
        val items = apiService.getInvitations().bodyOrThrow().mapNotNull { it.toDomain() }
        _invitations.value = items.toMutableList()
    }

    // ─── Contracts ─────────────────────────────────────────────────────────────

    private val _contracts = MutableStateFlow(mutableListOf<Contract>())
    val contracts: StateFlow<List<Contract>> = _contracts.asStateFlow()

    fun getContractByInquilino(inquilinoId: String) =
        _contracts.value.find { it.inquilinoId == inquilinoId && it.estado == ContractStatus.ACTIVO }

    fun getContractsByOwner(duenoId: String) = _contracts.value.filter { it.duenoId == duenoId }

    fun addContract(contract: Contract) {
        _contracts.update { list -> (list + contract).toMutableList() }
    }

    suspend fun refreshContracts(): Result<Unit> = runApiCall {
        val items = apiService.getContracts().bodyOrThrow().mapNotNull { it.toDomain() }
        _contracts.value = items.toMutableList()
    }

    // ─── Payments ──────────────────────────────────────────────────────────────

    private val _payments = MutableStateFlow(mutableListOf<Payment>())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    fun getPaymentsByContract(contratoId: String) = _payments.value.filter { it.contratoId == contratoId }
    fun getPaymentsByOwner(duenoId: String) = _payments.value.filter { it.duenoId == duenoId }
    fun getPendingPaymentsByOwner(duenoId: String) =
        _payments.value.filter { it.duenoId == duenoId && it.estado == PaymentStatus.PENDIENTE }

    fun addPayment(payment: Payment) {
        _payments.update { list -> (list + payment).toMutableList() }
    }

    fun approvePayment(id: String) {
        val now = System.currentTimeMillis().toString()
        _payments.update { list ->
            list.map { if (it.id == id) it.copy(estado = PaymentStatus.APROBADO, fechaRevision = now) else it }
                .toMutableList()
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

    suspend fun refreshPayments(userId: String): Result<Unit> = runApiCall {
        val items = apiService.getPaymentsByUser(userId).bodyOrThrow().mapNotNull { it.toDomain() }
        _payments.value = items.toMutableList()
    }

    // ─── Notifications (MS Notificaciones) ─────────────────────────────────────

    private val _notifications = MutableStateFlow(mutableListOf<AppNotification>())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun getNotificationsByUser(userId: String) = _notifications.value.filter { it.userId == userId }
    fun getUnreadCountByUser(userId: String) = _notifications.value.count { it.userId == userId && !it.leida }

    fun addNotification(notification: AppNotification) {
        _notifications.update { list -> (listOf(notification) + list).toMutableList() }
    }

    fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(leida = true) else it }.toMutableList()
        }
    }

    fun markAllNotificationsRead(userId: String) {
        _notifications.update { list ->
            list.map { if (it.userId == userId) it.copy(leida = true) else it }.toMutableList()
        }
    }

    suspend fun refreshNotifications(userId: String): Result<Unit> = runApiCall {
        val response = apiService.getNotificacionesByUser(userId).bodyOrThrow()
        val items = response.items.mapNotNull { it.toDomain() }
        _notifications.value = items.toMutableList()
    }

    suspend fun markNotificationReadApi(notificacionId: String): Result<Unit> = runApiCall {
        apiService.marcarNotificacionLeida(notificacionId, UpdateNotificationRequest(leida = true))
        markNotificationRead(notificacionId)
    }

    // ─── Messages + Conversations (MS Mensajes) ────────────────────────────────

    private val _conversations = MutableStateFlow(listOf<Conversation>())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow(mutableListOf<Message>())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun getConversationsByUser(userId: String) = _conversations.value.filter { userId in it.participants }

    fun getMessagesByConversation(conversationId: String) =
        _messages.value.filter { it.conversationId == conversationId }

    fun addLocalMessage(message: Message) {
        _messages.update { list -> (list + message).toMutableList() }
    }

    fun markMessagesAsRead(conversationId: String, userId: String) {
        _conversations.update { list ->
            list.map {
                if (it.id == conversationId) {
                    val newUnread = it.unreadCount.toMutableMap()
                    newUnread[userId] = 0
                    it.copy(unreadCount = newUnread)
                } else it
            }.toMutableList()
        }
    }

    fun getOrCreateConversation(user1Id: String, user2Id: String, propertyId: String? = null): Conversation {
        val existing = _conversations.value.find { user1Id in it.participants && user2Id in it.participants }
        if (existing != null) return existing
        return Conversation(
            id = "conv-${System.currentTimeMillis()}",
            participants = listOf(user1Id, user2Id),
            propertyId = propertyId,
            createdAt = System.currentTimeMillis().toString()
        )
    }

    suspend fun refreshConversations(userId: String): Result<Unit> = runApiCall {
        val response = apiService.getConversacionesByUser(userId).bodyOrThrow()
        val convs = response.conversaciones.mapNotNull { it.toDomain(userId) }
        _conversations.value = convs
    }

    suspend fun refreshMessages(conversationId: String): Result<Unit> = runApiCall {
        val response = apiService.getHistorialMensajes(conversationId).bodyOrThrow()
        val msgs = response.mensajes.mapNotNull { it.toDomain(conversationId) }
        _messages.update { current ->
            (current.filter { it.conversationId != conversationId } + msgs).toMutableList()
        }
    }

    suspend fun sendMessageApi(
        destinatarioId: String,
        propiedadId: String,
        contenido: String,
        arrendadorId: String,
        arrendatarioId: String
    ): Result<Message> = runApiCall {
        val response = apiService.enviarMensaje(
            MsMensajesEnviarRequest(
                destinatario_id  = destinatarioId,
                propiedad_id     = propiedadId,
                contenido        = contenido,
                arrendador_id    = arrendadorId,
                arrendatario_id  = arrendatarioId
            )
        ).bodyOrThrow()
        val msg = response.datos.toDomain()
            ?: throw Exception("Respuesta inválida al enviar mensaje")
        _messages.update { list -> (list + msg).toMutableList() }
        msg
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private fun <T> retrofit2.Response<T>.bodyOrThrow(): T =
        if (isSuccessful) body() ?: throw Exception("Respuesta vacía (${code()})")
        else throw Exception(errorMessageFor(code()))

    private fun errorMessageFor(code: Int) = when (code) {
        401  -> "🔐 Sesión expirada, volvé a iniciar sesión"
        403  -> "🚫 No tenés permiso para esto"
        404  -> "🔍 Recurso no encontrado"
        503  -> "🏖️ El microservicio está de vacaciones — volvé pronto"
        else -> "💥 Error $code del servidor"
    }

    private suspend fun <T> runApiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
}
