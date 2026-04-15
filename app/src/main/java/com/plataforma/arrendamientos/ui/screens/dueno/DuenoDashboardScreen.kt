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
import com.plataforma.arrendamientos.ui.components.*
import com.plataforma.arrendamientos.ui.navigation.Screen
import com.plataforma.arrendamientos.ui.theme.LocalIsDarkTheme
import com.plataforma.arrendamientos.ui.theme.LocalToggleTheme
import com.plataforma.arrendamientos.ui.theme.StatusAmber
import com.plataforma.arrendamientos.ui.theme.StatusAmberContainer
import com.plataforma.arrendamientos.ui.theme.StatusGreen
import com.plataforma.arrendamientos.ui.theme.StatusGreenContainer
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PaymentViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuenoDashboardScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val isDarkTheme = LocalIsDarkTheme.current
    val toggleTheme = LocalToggleTheme.current

    val myProperties = propertyViewModel.getPropertiesByOwner(user.id)
    val pendingPayments = paymentViewModel.getPendingPayments(user.id)
    val allPayments = paymentViewModel.getPaymentsByOwner(user.id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${user.nombre.split(" ").first()}!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Panel de propietario", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = toggleTheme) {
                        Icon(
                            if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Cambiar tema"
                        )
                    }
                    IconButton(onClick = { onNavigate(Screen.NotificacionesDueno.route) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { onNavigate(Screen.PerfilDueno.route) }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(Screen.MisPropiedades.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Propiedades") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(Screen.PagosRecibidos.route) },
                    icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                    label = { Text("Pagos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(Screen.MensajesDueno.route) },
                    icon = { Icon(Icons.Default.Message, contentDescription = null) },
                    label = { Text("Mensajes") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Propiedades",
                        value = "${myProperties.size}",
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pagos pendientes",
                        value = "${pendingPayments.size}",
                        icon = Icons.Default.Pending,
                        iconTint = StatusAmber,
                        iconContainerColor = StatusAmberContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val alquiladas = myProperties.count { it.estado.name == "ALQUILADA" }
                    StatCard(
                        title = "Alquiladas",
                        value = "$alquiladas",
                        icon = Icons.Default.Key,
                        iconTint = StatusGreen,
                        iconContainerColor = StatusGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total pagos",
                        value = "${allPayments.size}",
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                SectionHeader(title = "Acciones rápidas")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Add,
                        label = "Nueva\npropiedad",
                        onClick = { onNavigate(Screen.NuevaPropiedad.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.PersonAdd,
                        label = "Enviar\ninvitación",
                        onClick = { onNavigate(Screen.NuevaInvitacion.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.List,
                        label = "Ver\ninvitaciones",
                        onClick = { onNavigate(Screen.Invitaciones.route) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        icon = Icons.Default.History,
                        label = "Historial",
                        onClick = { onNavigate(Screen.HistorialDueno.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pending payments
            if (pendingPayments.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Pagos por revisar",
                        action = "Ver todos",
                        onAction = { onNavigate(Screen.PagosRecibidos.route) }
                    )
                }
                items(pendingPayments.take(3), key = { it.id }) { payment ->
                    Card(
                        onClick = { onNavigate(Screen.PagosRecibidos.route) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = StatusAmberContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = payment.tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${MONTH_NAMES.getOrNull(payment.mes - 1)} ${payment.anio}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatPrice(payment.monto, payment.moneda),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                PaymentStatusBadge(payment.estado)
                            }
                        }
                    }
                }
            }

            // My properties
            item {
                SectionHeader(
                    title = "Mis propiedades",
                    action = "Ver todas",
                    onAction = { onNavigate(Screen.MisPropiedades.route) }
                )
            }
            if (myProperties.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Home,
                        title = "Sin propiedades",
                        subtitle = "Agrega tu primera propiedad para empezar a arrendar.",
                        action = "Agregar propiedad",
                        onAction = { onNavigate(Screen.NuevaPropiedad.route) }
                    )
                }
            } else {
                items(myProperties.take(2), key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onClick = {},
                        showActions = true,
                        onEdit = { onNavigate(Screen.EditarPropiedad.createRoute(property.id)) },
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
