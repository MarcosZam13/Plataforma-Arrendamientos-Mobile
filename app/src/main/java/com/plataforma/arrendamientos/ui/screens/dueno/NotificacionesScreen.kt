package com.plataforma.arrendamientos.ui.screens.dueno
 
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.plataforma.arrendamientos.data.model.AppNotification
import com.plataforma.arrendamientos.data.model.NotificationType
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.ui.navigation.Screen
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.NotificationViewModel
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val allNotifications by notificationViewModel.notifications.collectAsState()
 
    val myNotifications = allNotifications.filter { it.userId == user.id }
    val unreadCount = myNotifications.count { !it.leida }
 
    var filterUnread by remember { mutableStateOf(false) }
    val displayed = if (filterUnread) myNotifications.filter { !it.leida } else myNotifications
 
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notificaciones")
                        if (unreadCount > 0) {
                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) {
                                Text(
                                    "$unreadCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { notificationViewModel.markAllAsRead(user.id) }) {
                            Text("Marcar todas", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = !filterUnread, onClick = { filterUnread = false }, label = { Text("Todas") })
                FilterChip(
                    selected = filterUnread,
                    onClick = { filterUnread = true },
                    label = { Text("No leídas${if (unreadCount > 0) " ($unreadCount)" else ""}") }
                )
            }
 
            if (displayed.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = if (filterUnread) "Sin notificaciones nuevas" else "Sin notificaciones",
                        subtitle = "Las notificaciones aparecerán aquí."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayed, key = { it.id }) { notif ->
                        NotificationCard(
                            notif = notif,
                            onMarkRead = { notificationViewModel.markAsRead(notif.id) },
                            onTap = {
                                notificationViewModel.markAsRead(notif.id)
                                val route = notifRoute(notif.tipo, isDueno = true)
                                if (route != null) onNavigate(route)
                            }
                        )
                    }
                }
            }
        }
    }
}
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesInquilinoScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val allNotifications by notificationViewModel.notifications.collectAsState()
 
    val myNotifications = allNotifications.filter { it.userId == user.id }
    val unreadCount = myNotifications.count { !it.leida }
 
    var filterUnread by remember { mutableStateOf(false) }
    val displayed = if (filterUnread) myNotifications.filter { !it.leida } else myNotifications
 
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notificaciones")
                        if (unreadCount > 0) {
                            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) {
                                Text(
                                    "$unreadCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { notificationViewModel.markAllAsRead(user.id) }) {
                            Text("Marcar todas", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = !filterUnread, onClick = { filterUnread = false }, label = { Text("Todas") })
                FilterChip(
                    selected = filterUnread,
                    onClick = { filterUnread = true },
                    label = { Text("No leídas${if (unreadCount > 0) " ($unreadCount)" else ""}") }
                )
            }
 
            if (displayed.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = if (filterUnread) "Sin notificaciones nuevas" else "Sin notificaciones",
                        subtitle = "Las notificaciones aparecerán aquí."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayed, key = { it.id }) { notif ->
                        NotificationCard(
                            notif = notif,
                            onMarkRead = { notificationViewModel.markAsRead(notif.id) },
                            onTap = {
                                notificationViewModel.markAsRead(notif.id)
                                val route = notifRoute(notif.tipo, isDueno = false)
                                if (route != null) onNavigate(route)
                            }
                        )
                    }
                }
            }
        }
    }
}
 
@Composable
private fun NotificationCard(
    notif: AppNotification,
    onMarkRead: () -> Unit,
    onTap: () -> Unit
) {
    val (iconRes, iconColor, iconContainer) = when (notif.tipo) {
        NotificationType.PAGO_RECIBIDO, NotificationType.PAGO_APROBADO ->
            Triple(Icons.Default.Payment, StatusGreen, StatusGreenContainer)
        NotificationType.PAGO_RECHAZADO ->
            Triple(Icons.Default.Cancel, StatusRed, StatusRedContainer)
        NotificationType.INVITACION_ENVIADA, NotificationType.INVITACION_ACEPTADA ->
            Triple(Icons.Default.MailOutline, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        NotificationType.MENSAJE_NUEVO ->
            Triple(Icons.Default.Message, StatusAmber, StatusAmberContainer)
        NotificationType.CONTRATO_ACTIVO, NotificationType.CONTRATO_FINALIZADO ->
            Triple(Icons.Default.Description, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
        else -> Triple(Icons.Default.Info, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }
 
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = if (!notif.leida)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(color = iconContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconRes, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(notif.mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!notif.leida) {
                IconButton(onClick = onMarkRead, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
 
private fun notifRoute(tipo: NotificationType, isDueno: Boolean): String? = when (tipo) {
    NotificationType.PAGO_RECIBIDO -> if (isDueno) Screen.PagosRecibidos.route else null
    NotificationType.PAGO_APROBADO, NotificationType.PAGO_RECHAZADO ->
        if (isDueno) null else Screen.HistorialInquilino.route
    NotificationType.INVITACION_ENVIADA, NotificationType.INVITACION_ACEPTADA ->
        if (isDueno) Screen.Invitaciones.route else null
    NotificationType.MENSAJE_NUEVO ->
        if (isDueno) Screen.MensajesDueno.route else Screen.MensajesInquilino.route
    NotificationType.CONTRATO_ACTIVO, NotificationType.CONTRATO_FINALIZADO ->
        if (isDueno) null else Screen.MiContrato.route
    else -> null
}