package com.plataforma.arrendamientos.ui.screens.inquilino
 
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.plataforma.arrendamientos.data.model.ContractStatus
import com.plataforma.arrendamientos.data.model.DepositStatus
import com.plataforma.arrendamientos.data.model.MockData
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.ui.components.formatPrice
import com.plataforma.arrendamientos.ui.theme.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel
 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiContratoScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val context = LocalContext.current
 
    val contract = MockData.MOCK_CONTRACT.takeIf { it.inquilinoId == user.id || true }
    val property = contract?.let { propertyViewModel.getPropertyById(it.propiedadId) }
 
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi contrato") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (contract != null) {
                        IconButton(onClick = {
                            val prop = property
                            val text = buildString {
                                appendLine("CONTRATO DE ARRENDAMIENTO")
                                appendLine("=========================")
                                if (prop != null) {
                                    appendLine("Propiedad: ${prop.titulo}")
                                    appendLine("Ubicación: ${prop.canton}, ${prop.provincia}")
                                }
                                appendLine()
                                appendLine("ID contrato: #${contract.id.takeLast(6)}")
                                appendLine("Fecha de inicio: ${contract.fechaInicio}")
                                appendLine("Estado: ${contract.estado.name}")
                                appendLine()
                                appendLine("Monto mensual: ${formatPrice(contract.montoMensual, contract.moneda)}")
                                appendLine("Depósito: ${formatPrice(contract.montoDeposito, contract.moneda)}")
                                appendLine("Estado del depósito: ${when (contract.estadoDeposito) {
                                    DepositStatus.PENDIENTE -> "Pendiente"
                                    DepositStatus.PAGADO -> "Pagado"
                                    DepositStatus.DEVUELTO -> "Devuelto"
                                    DepositStatus.RETENIDO -> "Retenido"
                                }}")
                                appendLine()
                                appendLine("Generado desde Plataforma de Arrendamientos CR")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Mi contrato de arrendamiento")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir contrato"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir contrato")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (contract == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.Description,
                    title = "Sin contrato activo",
                    subtitle = "No tienes ningún contrato de arrendamiento activo."
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (contract.estado) {
                            ContractStatus.ACTIVO -> StatusGreenContainer
                            ContractStatus.FINALIZADO -> MaterialTheme.colorScheme.surfaceVariant
                            ContractStatus.CANCELADO -> StatusRedContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Estado del contrato", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                when (contract.estado) {
                                    ContractStatus.ACTIVO -> "Contrato activo"
                                    ContractStatus.FINALIZADO -> "Finalizado"
                                    ContractStatus.CANCELADO -> "Cancelado"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (contract.estado) {
                                    ContractStatus.ACTIVO -> StatusGreen
                                    ContractStatus.FINALIZADO -> MaterialTheme.colorScheme.onSurface
                                    ContractStatus.CANCELADO -> StatusRed
                                }
                            )
                        }
                        Icon(
                            when (contract.estado) {
                                ContractStatus.ACTIVO -> Icons.Default.CheckCircle
                                ContractStatus.FINALIZADO -> Icons.Default.Done
                                ContractStatus.CANCELADO -> Icons.Default.Cancel
                            },
                            null,
                            tint = when (contract.estado) {
                                ContractStatus.ACTIVO -> StatusGreen
                                ContractStatus.FINALIZADO -> MaterialTheme.colorScheme.onSurface
                                ContractStatus.CANCELADO -> StatusRed
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
 
                // Property info
                property?.let { prop ->
                    Card(shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Propiedad arrendada", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(prop.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${prop.canton}, ${prop.provincia}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
 
                // Contract details
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Detalles del contrato", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        HorizontalDivider()
                        ContractDetailRow("Fecha de inicio", contract.fechaInicio)
                        ContractDetailRow("Monto mensual", formatPrice(contract.montoMensual, contract.moneda))
                        ContractDetailRow("Depósito", formatPrice(contract.montoDeposito, contract.moneda))
 
                        // Deposit status with color indicator
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Estado depósito", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val (depositText, depositColor) = when (contract.estadoDeposito) {
                                DepositStatus.PENDIENTE -> "Pendiente" to StatusAmber
                                DepositStatus.PAGADO -> "Pagado" to StatusGreen
                                DepositStatus.DEVUELTO -> "Devuelto" to MaterialTheme.colorScheme.primary
                                DepositStatus.RETENIDO -> "Retenido" to StatusRed
                            }
                            Text(depositText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = depositColor)
                        }
 
                        ContractDetailRow("ID contrato", "#${contract.id.takeLast(6)}")
                    }
                }
 
                // Next payment card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tu próximo pago", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatPrice(contract.montoMensual, contract.moneda), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
 
                // Share button
                OutlinedButton(
                    onClick = {
                        val prop = property
                        val text = buildString {
                            appendLine("CONTRATO DE ARRENDAMIENTO")
                            appendLine("=========================")
                            if (prop != null) appendLine("Propiedad: ${prop.titulo}")
                            appendLine("Monto mensual: ${formatPrice(contract.montoMensual, contract.moneda)}")
                            appendLine("Estado: ${contract.estado.name}")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Mi contrato de arrendamiento")
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir contrato"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir / Descargar contrato")
                }
 
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
 
@Composable
private fun ContractDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
 