package com.plataforma.arrendamientos.ui.screens.dueno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.MessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensajesScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    messageViewModel: MessageViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val conversations by messageViewModel.conversations.collectAsState()
    val myConversations = messageViewModel.getConversationsByUser(user.id)

    var selectedConversation by remember { mutableStateOf<String?>(null) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    if (selectedConversation == null) {
        // Conversation list
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mensajes") },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
                )
            }
        ) { padding ->
            if (myConversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Default.ChatBubbleOutline, title = "Sin conversaciones", subtitle = "Las conversaciones con tus inquilinos aparecerán aquí.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(myConversations, key = { it.id }) { conv ->
                        ListItem(
                            headlineContent = { Text(conv.otherUserName.ifBlank { "Inquilino" }, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text(conv.lastMessage ?: "Sin mensajes", maxLines = 1) },
                            leadingContent = {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            modifier = Modifier.clip(RoundedCornerShape(0.dp)),
                            trailingContent = {
                                val count = conv.unreadCount[user.id] ?: 0
                                if (count > 0) {
                                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(22.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    } else {
        // Chat view
        val messages = messageViewModel.getMessagesByConversation(selectedConversation!!)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Conversación") },
                    navigationIcon = {
                        IconButton(onClick = { selectedConversation = null }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    }
                )
            },
            bottomBar = {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Escribe un mensaje...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    messageViewModel.sendMessage(
                                        conversationId = selectedConversation!!,
                                        senderId = user.id,
                                        receiverId = "other",
                                        content = messageText
                                    )
                                    messageText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isMine = message.senderId == user.id
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(
                                topStart = if (isMine) 12.dp else 2.dp,
                                topEnd = if (isMine) 2.dp else 12.dp,
                                bottomStart = 12.dp,
                                bottomEnd = 12.dp
                            )
                        ) {
                            Text(
                                text = message.content,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
