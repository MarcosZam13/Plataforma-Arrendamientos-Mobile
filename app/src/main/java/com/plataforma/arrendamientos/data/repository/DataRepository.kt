package com.plataforma.arrendamientos.data.repository

import android.util.Log
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.remote.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DataRepository"

@Singleton
class DataRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ─── Properties ────────────────────────────────────────────────────────────

    private val _properties = MutableStateFlow(MockData.MOCK_PROPERTIES.toMutableList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    fun getPropertiesByOwner(duenoId: String) =
        _properties.value.filter { it.duenoId == duenoId }

    fun getPropertyById(id: String) = _properties.value.find { it.id == id }

    fun addProperty(property: Property) {
        _properties.update { list -> (list + property).toMutableList() }
        scope.launch {
            runCatching { apiService.createProperty(property.toCreateRequest()) }
                .onFailure { Log.w(TAG, "createProperty failed: ${it.message}") }
        }
    }

    fun updateProperty(property: Property) {
        _properties.update { list ->
            list.map { if (it.id == property.id) property else it }.toMutableList()
        }
        scope.launch {
            runCatching {
                apiService.updateProperty(
                    property.id,
                    UpdatePropertyRequest(
                        titulo = property.titulo,
                        descripcion = property.descripcion,
                        precio = property.precio,
                        estado = property.estado.name.lowercase()
                    )
                )
            }.onFailure { Log.w(TAG, "updateProperty failed: ${it.message}") }
        }
    }

    fun deleteProperty(id: String) {
        _properties.update { list -> list.filter { it.id != id }.toMutableList() }
        scope.launch {
            runCatching { apiService.deleteProperty(id) }
                .onFailure { Log.w(TAG, "deleteProperty failed: ${it.message}") }
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
        _invitations.update { list -> (list + invitation).toMutableList() }
        scope.launch {
            runCatching { apiService.createInvitation(invitation.toCreateRequest()) }
                .onFailure { Log.w(TAG, "createInvitation failed: ${it.message}") }
        }
    }

    fun updateInvitation(invitation: Invitation) {
        _invitations.update { list ->
            list.map { if (it.id == invitation.id) invitation else it }.toMutableList()
        }
        scope.launch {
            runCatching {
                apiService.updateInvitation(
                    invitation.id,
                    UpdateInvitationRequest(estado = invitation.estado.name.lowercase())
                )
            }.onFailure { Log.w(TAG, "updateInvitation failed: ${it.message}") }
        }
    }

    fun cancelInvitation(id: String) {
        _invitations.update { list ->
            list.map {
                if (it.id == id) it.copy(estado = InvitationStatus.CANCELADA) else it
            }.toMutableList()
        }
        scope.launch {
            runCatching {
                apiService.updateInvitation(id, UpdateInvitationRequest(estado = "cancelada"))
            }.onFailure { Log.w(TAG, "cancelInvitation failed: ${it.message}") }
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
        _contracts.update { list -> (list + contract).toMutableList() }
        scope.launch {
            runCatching { apiService.createContract(contract.toCreateRequest()) }
                .onFailure { Log.w(TAG, "createContract failed: ${it.message}") }
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
        _payments.update { list -> (list + payment).toMutableList() }
        scope.launch {
            runCatching { apiService.createPayment(payment.toCreateRequest()) }
                .onFailure { Log.w(TAG, "createPayment failed: ${it.message}") }
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
        scope.launch {
            runCatching { apiService.updatePayment(id, UpdatePaymentRequest(estado = "aprobado")) }
                .onFailure { Log.w(TAG, "approvePayment failed: ${it.message}") }
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
        scope.launch {
            runCatching {
                apiService.updatePayment(id, UpdatePaymentRequest(estado = "rechazado", motivoRechazo = motivo))
            }.onFailure { Log.w(TAG, "rejectPayment failed: ${it.message}") }
        }
    }

    // ─── Notifications ─────────────────────────────────────────────────────────

    private val _notifications = MutableStateFlow(mutableListOf<AppNotification>())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun getNotificationsByUser(userId: String) =
        _notifications.value.filter { it.userId == userId }

    fun addNotification(notification: AppNotification) {
        _notifications.update { list -> (listOf(notification) + list).toMutableList() }
        scope.launch {
            runCatching { apiService.createNotification(notification.toCreateRequest()) }
                .onFailure { Log.w(TAG, "createNotification failed: ${it.message}") }
        }
    }

    fun markNotificationRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(leida = true) else it }.toMutableList()
        }
        scope.launch {
            runCatching {
                apiService.updateNotification(id, UpdateNotificationRequest(leida = true))
            }.onFailure { Log.w(TAG, "markNotificationRead failed: ${it.message}") }
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
        _messages.update { list -> (list + message).toMutableList() }
        _conversations.update { list ->
            list.map {
                if (it.id == message.conversationId) it.copy(
                    lastMessage = message.content,
                    lastMessageAt = message.timestamp
                ) else it
            }.toMutableList()
        }
        scope.launch {
            runCatching {
                apiService.createMessage(
                    CreateMessageRequest(
                        conversationId = message.conversationId,
                        senderId = message.senderId,
                        receiverId = message.receiverId,
                        content = message.content,
                        type = message.type.name.lowercase()
                    )
                )
            }.onFailure { Log.w(TAG, "sendMessage failed: ${it.message}") }
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
        scope.launch {
            runCatching {
                apiService.createConversation(
                    CreateConversationRequest(
                        participants = listOf(user1Id, user2Id),
                        propertyId = propertyId
                    )
                )
            }.onFailure { Log.w(TAG, "createConversation failed: ${it.message}") }
        }
        return conv
    }

    // ─── API Data Loading ──────────────────────────────────────────────────────

    init {
        scope.launch { loadPropertiesFromApi() }
        scope.launch { loadInvitationsFromApi() }
        scope.launch { loadContractsFromApi() }
    }

    fun loadDataForUser(userId: String) {
        scope.launch { loadPaymentsFromApi(userId) }
        scope.launch { loadNotificationsFromApi(userId) }
        scope.launch { loadConversationsFromApi(userId) }
        scope.launch { loadMessagesFromApi(userId) }
    }

    fun clearUserData() {
        _payments.value = MockData.MOCK_PAYMENTS.toMutableList()
        _notifications.value = mutableListOf()
        _conversations.value = mutableListOf()
        _messages.value = mutableListOf()
    }

    private suspend fun loadPropertiesFromApi() {
        runCatching {
            val response = apiService.getProperties()
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                if (items.isNotEmpty()) {
                    _properties.value = items.toMutableList()
                }
            }
        }.onFailure { Log.w(TAG, "loadProperties failed: ${it.message}") }
    }

    private suspend fun loadInvitationsFromApi() {
        runCatching {
            val response = apiService.getInvitations()
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                if (items.isNotEmpty()) {
                    _invitations.value = items.toMutableList()
                }
            }
        }.onFailure { Log.w(TAG, "loadInvitations failed: ${it.message}") }
    }

    private suspend fun loadContractsFromApi() {
        runCatching {
            val response = apiService.getContracts()
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                if (items.isNotEmpty()) {
                    _contracts.value = items.toMutableList()
                }
            }
        }.onFailure { Log.w(TAG, "loadContracts failed: ${it.message}") }
    }

    private suspend fun loadPaymentsFromApi(userId: String) {
        runCatching {
            val response = apiService.getPaymentsByUser(userId)
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                if (items.isNotEmpty()) {
                    _payments.value = items.toMutableList()
                }
            }
        }.onFailure { Log.w(TAG, "loadPayments failed: ${it.message}") }
    }

    private suspend fun loadNotificationsFromApi(userId: String) {
        runCatching {
            val response = apiService.getNotificationsByUser(userId)
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                _notifications.value = items.toMutableList()
            }
        }.onFailure { Log.w(TAG, "loadNotifications failed: ${it.message}") }
    }

    private suspend fun loadConversationsFromApi(userId: String) {
        runCatching {
            val response = apiService.getConversationsByUser(userId)
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                if (items.isNotEmpty()) {
                    _conversations.value = items.toMutableList()
                }
            }
        }.onFailure { Log.w(TAG, "loadConversations failed: ${it.message}") }
    }

    private suspend fun loadMessagesFromApi(userId: String) {
        runCatching {
            val response = apiService.getMessagesByUser(userId)
            if (response.isSuccessful) {
                val items = response.body()?.mapNotNull { it.toDomain() } ?: emptyList()
                if (items.isNotEmpty()) {
                    _messages.value = items.toMutableList()
                }
            }
        }.onFailure { Log.w(TAG, "loadMessages failed: ${it.message}") }
    }
}
