package com.plataforma.arrendamientos.data.repository

import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
        val response = apiService.getProperties(limit = 100).bodyOrThrow()
        val props = response.data.mapNotNull { it.toDomain() }
        _properties.value = props.toMutableList()
    }

    suspend fun createPropertyApi(property: Property): Result<Property> = runApiCall {
        val response = apiService.createProperty(property.toCreateRequest()).bodyOrThrow()
        val created = response.toDomain() ?: throw Exception("Error al crear propiedad")
        _properties.update { list -> (list + created).toMutableList() }
        created
    }

    suspend fun updatePropertyApi(property: Property): Result<Property> = runApiCall {
        val req = UpdatePropertyRequest(
            titulo = property.titulo,
            descripcion = property.descripcion,
            precio = property.precio,
            estado = property.estado.name.lowercase()
        )
        val response = apiService.updateProperty(property.id, req).bodyOrThrow()
        val updated = response.toDomain() ?: property
        updateProperty(updated)
        updated
    }

    suspend fun deletePropertyApi(id: String): Result<Unit> = runApiCall {
        apiService.deleteProperty(id)
        deleteProperty(id)
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

    suspend fun refreshInvitations(userId: String? = null): Result<Unit> = runApiCall {
        val items = apiService.getInvitations().bodyOrThrow().mapNotNull { it.toDomain() }
        _invitations.value = if (userId != null) items.filter { it.duenoId == userId || it.inquilinoCorreo != null }.toMutableList()
                             else items.toMutableList()
    }

    suspend fun createInvitationApi(invitation: Invitation): Result<Invitation> = runApiCall {
        val response = apiService.createInvitation(invitation.toCreateRequest()).bodyOrThrow()
        val created = response.toDomain() ?: invitation
        addInvitation(created)
        created
    }

    suspend fun updateInvitationStatusApi(id: String, estado: String): Result<Unit> = runApiCall {
        apiService.updateInvitation(id, UpdateInvitationRequest(estado = estado))
        val inv = _invitations.value.find { it.id == id }
        if (inv != null) {
            val newStatus = when (estado.lowercase()) {
                "aceptada"  -> InvitationStatus.ACEPTADA
                "cancelada" -> InvitationStatus.CANCELADA
                else        -> inv.estado
            }
            updateInvitation(inv.copy(estado = newStatus))
        }
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

    fun getContractByUser(userId: String): Contract? =
        _contracts.value.find { it.inquilinoId == userId || it.duenoId == userId }

    suspend fun createContractApi(contract: Contract): Result<Contract> = runApiCall {
        val response = apiService.createContract(contract.toCreateRequest()).bodyOrThrow()
        val created = response.toDomain() ?: contract
        addContract(created)
        created
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

    suspend fun createPaymentApi(payment: Payment): Result<Payment> = runApiCall {
        val response = apiService.createPayment(payment.toCreateRequest()).bodyOrThrow()
        val created = response.toDomain() ?: payment
        addPayment(created)
        created
    }

    suspend fun updatePaymentStatusApi(id: String, estado: String, motivo: String? = null): Result<Unit> = runApiCall {
        apiService.updatePayment(id, UpdatePaymentRequest(estado = estado, motivoRechazo = motivo))
        when (estado.lowercase()) {
            "aprobado"  -> approvePayment(id)
            "rechazado" -> if (motivo != null) rejectPayment(id, motivo)
        }
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
        apiService.marcarNotificacionLeida(notificacionId)
        markNotificationRead(notificacionId)
    }

    suspend fun registrarDispositivo(userId: String, fcmToken: String): Result<Unit> = runApiCall {
        apiService.registrarDispositivo(
            com.plataforma.arrendamientos.data.remote.RegistrarDispositivoRequest(
                usuario_id = userId,
                fcm_token = fcmToken
            )
        )
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
        val response = apiService.getConversacionesByUser().bodyOrThrow()
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
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("⏳ El servidor tardó demasiado. Puede estar iniciando, intentá de nuevo."))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("📡 Sin conexión a internet. Verificá tu red."))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
