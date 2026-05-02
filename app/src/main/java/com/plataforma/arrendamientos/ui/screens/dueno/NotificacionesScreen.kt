package com.plataforma.arrendamientos.ui.screens.dueno

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
import com.plataforma.arrendamientos.data.model.NotificationType
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    NotificacionesContent(userId = user.id, isDueno = true, onBack = onBack)
}

@Composable
fun NotificacionesInquilinoScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    NotificacionesContent(userId = user.id, isDueno = false, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificacionesContent(userId: String, isDueno: Boolean, onBack: () -> Unit) {
    val notifications = remember(isDueno) {
        if (isDueno) listOf(
            Triple(NotificationType.PAGO_RECIBIDO,      "Nuevo comprobante de pago",  "El inquilino ha subido un comprobante de pago."),
            Triple(NotificationType.INVITACION_ACEPTADA, "Invitación aceptada",        "El inquilino aceptó tu invitación de arrendamiento."),
            Triple(NotificationType.MENSAJE_NUEVO,       "Nuevo mensaje",              "Tienes un mensaje nuevo de tu inquilino.")
        ) else listOf(
            Triple(NotificationType.PAGO_APROBADO,  "Pago aprobado",           "El propietario aprobó tu comprobante de pago."),
            Triple(NotificationType.MENSAJE_NUEVO,  "Nuevo mensaje",           "Tienes un mensaje nuevo de tu propietario."),
            Triple(NotificationType.CONTRATO_ACTIVO, "Contrato activo",         "Tu contrato de arrendamiento está vigente.")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(icon = Icons.Default.NotificationsNone, title = "Sin notificaciones", subtitle = "Las notificaciones aparecerán aquí.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications) { (tipo, titulo, mensaje) ->
                    val (iconRes, iconColor, iconContainer) = when (tipo) {
                        NotificationType.PAGO_RECIBIDO, NotificationType.PAGO_APROBADO -> Triple(Icons.Default.Payment, StatusGreen, StatusGreenContainer)
                        NotificationType.PAGO_RECHAZADO -> Triple(Icons.Default.Cancel, StatusRed, StatusRedContainer)
                        NotificationType.INVITACION_ENVIADA, NotificationType.INVITACION_ACEPTADA -> Triple(Icons.Default.MailOutline, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        NotificationType.MENSAJE_NUEVO -> Triple(Icons.Default.Message, StatusAmber, StatusAmberContainer)
                        NotificationType.CONTRATO_ACTIVO, NotificationType.CONTRATO_FINALIZADO -> Triple(Icons.Default.Description, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        else -> Triple(Icons.Default.Info, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    }
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                            Surface(color = iconContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(iconRes, null, tint = iconColor, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
