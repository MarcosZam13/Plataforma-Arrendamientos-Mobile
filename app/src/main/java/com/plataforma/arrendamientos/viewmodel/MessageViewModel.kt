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
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> = dataRepository.conversations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refreshConversations(userId: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            _error.update { null }
            dataRepository.refreshConversations(userId)
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun refreshMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            dataRepository.refreshMessages(conversationId)
                .onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            _isLoading.update { false }
        }
    }

    fun getConversationsByUser(userId: String) = dataRepository.getConversationsByUser(userId)

    fun getMessagesByConversation(conversationId: String) =
        dataRepository.getMessagesByConversation(conversationId)

    fun getOrCreateConversation(user1Id: String, user2Id: String, propertyId: String? = null) =
        dataRepository.getOrCreateConversation(user1Id, user2Id, propertyId)

    fun markConversationAsRead(conversationId: String, userId: String) {
        dataRepository.markMessagesAsRead(conversationId, userId)
    }

    fun getUnreadTotalForUser(userId: String): Int =
        dataRepository.getConversationsByUser(userId).sumOf { it.unreadCount[userId] ?: 0 }

    fun sendMessage(
        conversationId: String,
        senderId: String,
        receiverId: String,
        content: String,
        type: MessageType = MessageType.TEXT
    ) {
        viewModelScope.launch {
            val conv = dataRepository.conversations.value.find { it.id == conversationId }
            val propiedadId = conv?.propertyId ?: ""
            val arrendadorId = conv?.arrendadorId ?: ""
            val arrendatarioId = conv?.arrendatarioId ?: ""

            if (propiedadId.isNotBlank() && arrendadorId.isNotBlank() && arrendatarioId.isNotBlank()) {
                dataRepository.sendMessageApi(
                    destinatarioId = receiverId,
                    propiedadId = propiedadId,
                    contenido = content,
                    arrendadorId = arrendadorId,
                    arrendatarioId = arrendatarioId
                ).onFailure { e -> _error.update { e.message ?: "Error desconocido" } }
            } else {
                // Optimistic local fallback when conversation metadata is incomplete
                val message = Message(
                    id = "msg-${System.currentTimeMillis()}",
                    conversationId = conversationId,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    type = type,
                    status = MessageStatus.SENT,
                    timestamp = System.currentTimeMillis().toString()
                )
                dataRepository.addLocalMessage(message)
            }
        }
    }

    fun clearError() = _error.update { null }
}
