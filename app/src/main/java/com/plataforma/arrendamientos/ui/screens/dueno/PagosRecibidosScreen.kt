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
import com.plataforma.arrendamientos.data.model.Payment
import com.plataforma.arrendamientos.data.model.PaymentStatus
import com.plataforma.arrendamientos.ui.components.*
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagosRecibidosScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val payments by paymentViewModel.payments.collectAsState()
    val myPayments = payments.filter { it.duenoId == user.id }

    var filterStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var showRejectDialog by remember { mutableStateOf<Payment?>(null) }
    var rejectMotivo by remember { mutableStateOf("") }

    val filteredPayments = if (filterStatus == null) myPayments
        else myPayments.filter { it.estado == filterStatus }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagos recibidos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filterStatus == null, onClick = { filterStatus = null }, label = { Text("Todos") })
                FilterChip(selected = filterStatus == PaymentStatus.PENDIENTE, onClick = { filterStatus = PaymentStatus.PENDIENTE }, label = { Text("Pendientes") })
                FilterChip(selected = filterStatus == PaymentStatus.APROBADO, onClick = { filterStatus = PaymentStatus.APROBADO }, label = { Text("Aprobados") })
                FilterChip(selected = filterStatus == PaymentStatus.RECHAZADO, onClick = { filterStatus = PaymentStatus.RECHAZADO }, label = { Text("Rechazados") })
            }

            if (filteredPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Default.Receipt, title = "Sin pagos", subtitle = "No hay pagos en esta categoría.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPayments.reversed(), key = { it.id }) { payment ->
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = if (payment.tipo.name == "MENSUALIDAD") "Mensualidad" else "Depósito",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${MONTH_NAMES.getOrNull(payment.mes - 1)} ${payment.anio}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatPrice(payment.monto, payment.moneda),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        PaymentStatusBadge(payment.estado)
                                    }
                                }

                                if (payment.comprobante != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text("Comprobante adjunto", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                if (payment.motivoRechazo != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Motivo: ${payment.motivoRechazo}", style = MaterialTheme.typography.bodySmall, color = StatusRed)
                                }

                                if (payment.estado == PaymentStatus.PENDIENTE) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { showRejectDialog = payment },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                                        ) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Rechazar")
                                        }
                                        Button(
                                            onClick = { paymentViewModel.approvePayment(payment.id, payment.inquilinoId) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                                        ) {
                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Aprobar")
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

    // Reject dialog
    showRejectDialog?.let { payment ->
        AlertDialog(
            onDismissRequest = { showRejectDialog = null; rejectMotivo = "" },
            title = { Text("Rechazar pago") },
            text = {
                Column {
                    Text("Indica el motivo del rechazo:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectMotivo,
                        onValueChange = { rejectMotivo = it },
                        placeholder = { Text("Ej: Comprobante ilegible, monto incorrecto...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        paymentViewModel.rejectPayment(payment.id, rejectMotivo, payment.inquilinoId)
                        showRejectDialog = null
                        rejectMotivo = ""
                    },
                    enabled = rejectMotivo.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Rechazar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRejectDialog = null; rejectMotivo = "" }) { Text("Cancelar") }
            }
        )
    }
}
