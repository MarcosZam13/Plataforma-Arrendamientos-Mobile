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
import com.plataforma.arrendamientos.data.model.InvitationStatus
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.ui.components.StatusBadge
import com.plataforma.arrendamientos.ui.components.formatPrice
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.InvitationViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitacionesScreen(
    onNewInvitation: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    invitationViewModel: InvitationViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return

    var refreshKey by remember { mutableStateOf(0) }
    val invitations = remember(refreshKey) { invitationViewModel.getInvitationsByOwner(user.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitaciones") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { refreshKey++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewInvitation,
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text("Nueva invitación") }
            )
        }
    ) { padding ->
        if (invitations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.MailOutline,
                    title = "Sin invitaciones",
                    subtitle = "Envía una invitación a un inquilino para que pueda ver tu propiedad.",
                    action = "Nueva invitación",
                    onAction = onNewInvitation
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(invitations.reversed(), key = { it.id }) { invitation ->
                    val property = propertyViewModel.getPropertyById(invitation.propiedadId)
                    val (statusText, statusColor, statusContainer) = when (invitation.estado) {
                        InvitationStatus.PENDIENTE -> Triple("Pendiente", StatusAmber, StatusAmberContainer)
                        InvitationStatus.ACEPTADA -> Triple("Aceptada", StatusGreen, StatusGreenContainer)
                        InvitationStatus.EXPIRADA -> Triple("Expirada", OnSurfaceVariantLight, BackgroundLight)
                        InvitationStatus.CANCELADA -> Triple("Cancelada", StatusRed, StatusRedContainer)
                    }

                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = property?.titulo ?: "Propiedad",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = invitation.inquilinoCorreo ?: "Sin correo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(text = statusText, color = statusColor, containerColor = statusContainer)
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Monto mensual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatPrice(invitation.montoAlquiler, invitation.moneda), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Depósito", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(formatPrice(invitation.montoDeposito, invitation.moneda), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                                }
                            }

                            if (invitation.estado == InvitationStatus.PENDIENTE) {
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { invitationViewModel.cancelInvitation(invitation.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                                ) {
                                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cancelar invitación")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
