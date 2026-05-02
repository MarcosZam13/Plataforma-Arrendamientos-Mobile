package com.plataforma.arrendamientos.viewmodel

import androidx.lifecycle.ViewModel
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> = dataRepository.conversations

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
        dataRepository.sendMessage(message)
        dataRepository.addNotification(
            AppNotification(
                id = "notif-${System.currentTimeMillis()}",
                userId = receiverId,
                tipo = NotificationType.MENSAJE_NUEVO,
                titulo = "Nuevo mensaje",
                mensaje = content.take(50),
                leida = false,
                fecha = System.currentTimeMillis().toString()
            )
        )
    }
}
