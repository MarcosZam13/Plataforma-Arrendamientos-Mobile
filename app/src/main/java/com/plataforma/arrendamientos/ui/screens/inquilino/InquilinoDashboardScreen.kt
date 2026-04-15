package com.plataforma.arrendamientos.ui.screens.inquilino

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.data.model.PaymentStatus
import com.plataforma.arrendamientos.ui.components.*
import com.plataforma.arrendamientos.ui.navigation.Screen
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.ui.theme.LocalIsDarkTheme
import com.plataforma.arrendamientos.ui.theme.LocalToggleTheme
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PaymentViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquilinoDashboardScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val isDarkTheme = LocalIsDarkTheme.current
    val toggleTheme = LocalToggleTheme.current
    val payments by paymentViewModel.payments.collectAsState()

    val myPayments = payments.filter { it.inquilinoId == user.id }
    val pendingPayments = myPayments.filter { it.estado == PaymentStatus.PENDIENTE }
    val recentPayments = myPayments.take(3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${user.nombre.split(" ").first()}!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Panel de inquilino", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = toggleTheme) {
                        Icon(
                            if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema"
                        )
                    }
                    IconButton(onClick = { onNavigate(Screen.NotificacionesInquilino.route) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { onNavigate(Screen.PerfilInquilino.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = { onNavigate(Screen.MiContrato.route) }, icon = { Icon(Icons.Default.Description, null) }, label = { Text("Contrato") })
                NavigationBarItem(selected = false, onClick = { onNavigate(Screen.SubirComprobante.route) }, icon = { Icon(Icons.Default.Payment, null) }, label = { Text("Pagar") })
                NavigationBarItem(selected = false, onClick = { onNavigate(Screen.MensajesInquilino.route) }, icon = { Icon(Icons.Default.Message, null) }, label = { Text("Mensajes") })
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text("Contrato activo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text("Apartamento moderno en Escazú", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$ 950 / mes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Mis pagos",
                        value = "${myPayments.size}",
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pendientes",
                        value = "${pendingPayments.size}",
                        icon = Icons.Default.Pending,
                        iconTint = StatusAmber,
                        iconContainerColor = StatusAmberContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                SectionHeader(title = "Acciones rápidas")
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionItem(Icons.Default.Payment, "Subir\ncomprobante", { onNavigate(Screen.SubirComprobante.route) }, Modifier.weight(1f))
                    QuickActionItem(Icons.Default.Description, "Mi\ncontrato", { onNavigate(Screen.MiContrato.route) }, Modifier.weight(1f))
                    QuickActionItem(Icons.Default.Message, "Mensajes", { onNavigate(Screen.MensajesInquilino.route) }, Modifier.weight(1f))
                    QuickActionItem(Icons.Default.History, "Historial", { onNavigate(Screen.HistorialInquilino.route) }, Modifier.weight(1f))
                }
            }

            // Recent payments header
            item {
                SectionHeader(
                    title = "Mis pagos recientes",
                    action = "Ver todos",
                    onAction = { onNavigate(Screen.HistorialInquilino.route) }
                )
            }

            if (myPayments.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Receipt,
                        title = "Sin pagos",
                        subtitle = "Sube tu primer comprobante de pago.",
                        action = "Subir comprobante",
                        onAction = { onNavigate(Screen.SubirComprobante.route) }
                    )
                }
            } else {
                itemsIndexed(recentPayments, key = { _, item -> item.id }) { _, payment ->
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = when (payment.estado) {
                                        PaymentStatus.APROBADO -> StatusGreenContainer
                                        PaymentStatus.RECHAZADO -> StatusRedContainer
                                        else -> StatusAmberContainer
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            when (payment.estado) {
                                                PaymentStatus.APROBADO -> Icons.Default.CheckCircle
                                                PaymentStatus.RECHAZADO -> Icons.Default.Cancel
                                                else -> Icons.Default.Schedule
                                            },
                                            null,
                                            tint = when (payment.estado) {
                                                PaymentStatus.APROBADO -> StatusGreen
                                                PaymentStatus.RECHAZADO -> StatusRed
                                                else -> StatusAmber
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
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
