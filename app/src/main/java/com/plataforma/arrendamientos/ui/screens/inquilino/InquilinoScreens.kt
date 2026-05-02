package com.plataforma.arrendamientos.ui.screens.inquilino
 
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import com.plataforma.arrendamientos.data.model.PaymentStatus
import com.plataforma.arrendamientos.ui.components.*
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.MessageViewModel
import com.plataforma.arrendamientos.viewmodel.PaymentViewModel
 
// ─── Mensajes Inquilino ───────────────────────────────────────────────────────
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensajesInquilinoScreen(
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
                    EmptyState(
                        icon = Icons.Default.ChatBubbleOutline,
                        title = "Sin conversaciones",
                        subtitle = "Tus conversaciones con el propietario aparecerán aquí."
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(myConversations, key = { it.id }) { conv ->
                        val unread = conv.unreadCount[user.id] ?: 0
                        ListItem(
                            headlineContent = {
                                Text(
                                    conv.otherUserName.ifBlank { "Propietario" },
                                    fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    conv.lastMessage ?: "Sin mensajes",
                                    maxLines = 1,
                                    fontWeight = if (unread > 0) FontWeight.Medium else FontWeight.Normal
                                )
                            },
                            leadingContent = {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            trailingContent = {
                                if (unread > 0) {
                                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(22.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("$unread", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(0.dp))
                                .clickable {
                                    selectedConversation = conv.id
                                    messageViewModel.markConversationAsRead(conv.id, user.id)
                                }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    } else {
        val messages = messageViewModel.getMessagesByConversation(selectedConversation!!)
        val conv = myConversations.find { it.id == selectedConversation }
        val receiverId = conv?.participants?.firstOrNull { it != user.id } ?: ""
 
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(conv?.otherUserName?.ifBlank { "Conversación" } ?: "Conversación") },
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
                                        receiverId = receiverId,
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
 
// ─── Historial Inquilino ──────────────────────────────────────────────────────
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialInquilinoScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val payments by paymentViewModel.payments.collectAsState()
    val myPayments = payments.filter { it.inquilinoId == user.id }
 
    var filterStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var filterAnio by remember { mutableStateOf<Int?>(null) }
 
    val anios = myPayments.map { it.anio }.distinct().sortedDescending()
 
    val filteredPayments = myPayments
        .let { list -> if (filterStatus != null) list.filter { it.estado == filterStatus } else list }
        .let { list -> if (filterAnio != null) list.filter { it.anio == filterAnio } else list }
 
    val totalPaid = filteredPayments.filter { it.estado == PaymentStatus.APROBADO }.sumOf { it.monto }
    val currency = filteredPayments.firstOrNull()?.moneda ?: myPayments.firstOrNull()?.moneda
 
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de pagos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
 
            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total pagado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (currency != null) formatPrice(totalPaid, currency) else "$ 0",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${filteredPayments.filter { it.estado == PaymentStatus.APROBADO }.size} pagos aprobados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }
 
            // Filters — status
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filterStatus == null, onClick = { filterStatus = null }, label = { Text("Todos") })
                FilterChip(selected = filterStatus == PaymentStatus.APROBADO, onClick = { filterStatus = PaymentStatus.APROBADO }, label = { Text("Aprobados") })
                FilterChip(selected = filterStatus == PaymentStatus.PENDIENTE, onClick = { filterStatus = PaymentStatus.PENDIENTE }, label = { Text("Pendientes") })
                FilterChip(selected = filterStatus == PaymentStatus.RECHAZADO, onClick = { filterStatus = PaymentStatus.RECHAZADO }, label = { Text("Rechazados") })
            }
 
            // Filters — year
            if (anios.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = filterAnio == null, onClick = { filterAnio = null }, label = { Text("Todos los años") })
                    anios.forEach { anio ->
                        FilterChip(selected = filterAnio == anio, onClick = { filterAnio = anio }, label = { Text("$anio") })
                    }
                }
            }
 
            if (filteredPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Default.History, title = "Sin historial", subtitle = "Tus pagos aparecerán aquí una vez que los envíes.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPayments.reversed(), key = { it.id }) { payment ->
                        Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = when (payment.estado) {
                                                PaymentStatus.APROBADO -> StatusGreenContainer
                                                PaymentStatus.RECHAZADO -> StatusRedContainer
                                                PaymentStatus.PENDIENTE -> StatusAmberContainer
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    when (payment.estado) {
                                                        PaymentStatus.APROBADO -> Icons.Default.CheckCircle
                                                        PaymentStatus.RECHAZADO -> Icons.Default.Cancel
                                                        PaymentStatus.PENDIENTE -> Icons.Default.Schedule
                                                    },
                                                    null,
                                                    tint = when (payment.estado) {
                                                        PaymentStatus.APROBADO -> StatusGreen
                                                        PaymentStatus.RECHAZADO -> StatusRed
                                                        PaymentStatus.PENDIENTE -> StatusAmber
                                                    },
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                if (payment.tipo.name == "MENSUALIDAD") "Mensualidad" else "Depósito",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                "${MONTH_NAMES.getOrNull(payment.mes - 1)} ${payment.anio}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatPrice(payment.monto, payment.moneda), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        PaymentStatusBadge(payment.estado)
                                    }
                                }
                                // Motivo de rechazo visible para el inquilino
                                if (payment.motivoRechazo != null) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = StatusRed)
                                        Text(
                                            "Motivo de rechazo: ${payment.motivoRechazo}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
 