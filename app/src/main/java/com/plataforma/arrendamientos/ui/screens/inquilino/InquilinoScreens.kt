package com.plataforma.arrendamientos.ui.screens.inquilino

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.data.model.MockData
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
                        ListItem(
                            headlineContent = { Text(conv.otherUserName.ifBlank { "Propietario" }, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text(conv.lastMessage ?: "Sin mensajes", maxLines = 1) },
                            leadingContent = {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de pagos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary
            val totalPaid = myPayments.filter { it.estado.name == "APROBADO" }.sumOf { it.monto }
            val currency = myPayments.firstOrNull()?.moneda

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
                        Text("Pagos aprobados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
            }

            if (myPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Default.History, title = "Sin historial", subtitle = "Tus pagos aparecerán aquí una vez que los envíes.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myPayments.reversed(), key = { it.id }) { payment ->
                        Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = when (payment.estado.name) {
                                            "APROBADO" -> StatusGreenContainer
                                            "RECHAZADO" -> StatusRedContainer
                                            else -> StatusAmberContainer
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                when (payment.estado.name) {
                                                    "APROBADO" -> Icons.Default.CheckCircle
                                                    "RECHAZADO" -> Icons.Default.Cancel
                                                    else -> Icons.Default.Schedule
                                                },
                                                null,
                                                tint = when (payment.estado.name) {
                                                    "APROBADO" -> StatusGreen
                                                    "RECHAZADO" -> StatusRed
                                                    else -> StatusAmber
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(if (payment.tipo.name == "MENSUALIDAD") "Mensualidad" else "Depósito", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text("${MONTH_NAMES.getOrNull(payment.mes - 1)} ${payment.anio}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatPrice(payment.monto, payment.moneda), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    PaymentStatusBadge(payment.estado)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
