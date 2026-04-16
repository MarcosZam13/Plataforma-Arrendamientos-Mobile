package com.plataforma.arrendamientos.ui.screens.public_screens

import androidx.compose.foundation.layout.*
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
import com.plataforma.arrendamientos.data.model.InvitationStatus
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.ui.components.formatPrice
import com.plataforma.arrendamientos.ui.theme.StatusGreen
import com.plataforma.arrendamientos.ui.theme.StatusGreenContainer
import com.plataforma.arrendamientos.viewmodel.InvitationViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AceptarInvitacionScreen(
    token: String,
    currentUser: User?,
    onSuccess: () -> Unit,
    onLogin: () -> Unit,
    invitationViewModel: InvitationViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val invitation = invitationViewModel.getInvitationByToken(token)
    val property = invitation?.let { propertyViewModel.getPropertyById(it.propiedadId) }

    var isLoading by remember { mutableStateOf(false) }
    var accepted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitación de arrendamiento") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            when {
                invitation == null -> {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Invitación no encontrada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("El enlace de invitación no es válido o ha expirado.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }

                invitation.estado == InvitationStatus.EXPIRADA -> {
                    Text("Invitación expirada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Esta invitación ha expirado.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                invitation.estado == InvitationStatus.ACEPTADA || accepted -> {
                    Surface(
                        color = StatusGreenContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(36.dp), tint = StatusGreen)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("¡Invitación aceptada!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = StatusGreen)
                    Text("Tu contrato ha sido creado exitosamente.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onSuccess,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ir a mi contrato")
                    }
                }

                else -> {
                    // Valid pending invitation
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Has recibido una invitación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Un propietario te ha invitado a arrendar una propiedad.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                    Spacer(Modifier.height(24.dp))

                    // Property info card
                    property?.let { prop ->
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(prop.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("${prop.canton}, ${prop.provincia}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(12.dp))
                                InvitationDetailRow("Monto mensual", formatPrice(invitation.montoAlquiler, invitation.moneda))
                                InvitationDetailRow("Depósito", formatPrice(invitation.montoDeposito, invitation.moneda))
                                if (!invitation.notas.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Notas del propietario:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(invitation.notas, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                    }

                    if (currentUser == null) {
                        Text(
                            "Necesitas iniciar sesión para aceptar la invitación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onLogin,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Iniciar sesión para aceptar")
                        }
                    } else {
                        Button(
                            onClick = {
                                isLoading = true
                                val contract = invitationViewModel.acceptInvitation(token, currentUser.id)
                                if (contract != null) {
                                    accepted = true
                                } else {
                                    error = "No se pudo aceptar la invitación."
                                }
                                isLoading = false
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Aceptar invitación", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
