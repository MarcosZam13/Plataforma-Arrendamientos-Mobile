package com.plataforma.arrendamientos.ui.screens.dueno

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.data.model.PaymentStatus
import com.plataforma.arrendamientos.ui.components.*
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val payments by paymentViewModel.payments.collectAsState()
    val myPayments = payments.filter { it.duenoId == user.id }
    val context = LocalContext.current

    var filterStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var filterAnio by remember { mutableStateOf<Int?>(null) }

    val anios = myPayments.map { it.anio }.distinct().sortedDescending()

    val filteredPayments = myPayments
        .let { list -> if (filterStatus != null) list.filter { it.estado == filterStatus } else list }
        .let { list -> if (filterAnio != null) list.filter { it.anio == filterAnio } else list }

    val totalIncome = filteredPayments.filter { it.estado == PaymentStatus.APROBADO }.sumOf { it.monto }
    val currency = filteredPayments.firstOrNull()?.moneda ?: myPayments.firstOrNull()?.moneda

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de pagos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = {
                        val text = buildString {
                            appendLine("HISTORIAL DE PAGOS")
                            appendLine("==================")
                            filteredPayments.reversed().forEach { p ->
                                appendLine("${if (p.tipo.name == "MENSUALIDAD") "Mensualidad" else "Depósito"} - ${MONTH_NAMES.getOrNull(p.mes - 1)} ${p.anio}")
                                appendLine("  Monto: ${formatPrice(p.monto, p.moneda)}")
                                appendLine("  Estado: ${p.estado.name}")
                                if (p.motivoRechazo != null) appendLine("  Motivo: ${p.motivoRechazo}")
                                appendLine()
                            }
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Historial de pagos")
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar historial"))
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exportar")
                    }
                }
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
                        Text("Total ingresos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = if (currency != null) formatPrice(totalIncome, currency) else "₡ 0",
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
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        }
                    }
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
                    EmptyState(icon = Icons.Default.History, title = "Sin resultados", subtitle = "No hay pagos con los filtros seleccionados.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPayments.reversed(), key = { it.id }) { payment ->
                        Card(shape = RoundedCornerShape(10.dp)) {
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
                                if (payment.motivoRechazo != null) {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Motivo: ${payment.motivoRechazo}", style = MaterialTheme.typography.labelSmall, color = StatusRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
