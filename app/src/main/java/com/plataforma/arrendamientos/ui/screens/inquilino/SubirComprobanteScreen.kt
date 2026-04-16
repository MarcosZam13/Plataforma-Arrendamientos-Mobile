package com.plataforma.arrendamientos.ui.screens.inquilino

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plataforma.arrendamientos.data.model.Currency
import com.plataforma.arrendamientos.data.model.MockData
import com.plataforma.arrendamientos.data.model.PaymentType
import com.plataforma.arrendamientos.ui.theme.StatusGreen
import com.plataforma.arrendamientos.ui.theme.StatusGreenContainer
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PaymentViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubirComprobanteScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return

    val contract = MockData.MOCK_CONTRACT
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var notas by remember { mutableStateOf("") }

    val today = LocalDate.now()
    val currentMonth = today.monthValue
    val currentYear = today.year

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subir comprobante") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            if (submitted) {
                // Success state
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusGreenContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("¡Comprobante enviado!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusGreen)
                        Text("El propietario revisará tu comprobante y te notificará.", style = MaterialTheme.typography.bodyMedium, color = StatusGreen, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio") }
                    }
                }
            } else {
                // Payment info card
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
                            Text("Monto a pagar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "$ ${contract.montoMensual}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("${getMonthName(currentMonth)} $currentYear", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Payment, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                // Upload instructions
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Instrucciones de pago", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        InstructionRow("1", "Realiza el pago por SINPE Móvil o transferencia bancaria")
                        InstructionRow("2", "Toma una captura de pantalla del comprobante")
                        InstructionRow("3", "Sube la imagen del comprobante")
                        InstructionRow("4", "Espera la confirmación del propietario")
                    }
                }

                // Image picker
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Comprobante de pago", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))

                        if (imageUri != null) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Comprobante",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { imageUri = null },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(20.dp)) {
                                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Toca para seleccionar imagen", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (imageUri == null) "Seleccionar comprobante" else "Cambiar imagen")
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas adicionales (opcional)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Submit button
                Button(
                    onClick = {
                        isLoading = true
                        paymentViewModel.submitPayment(
                            contratoId = contract.id,
                            propiedadId = contract.propiedadId,
                            inquilinoId = user.id,
                            duenoId = contract.duenoId,
                            monto = contract.montoMensual,
                            moneda = contract.moneda,
                            mes = currentMonth,
                            anio = currentYear,
                            comprobante = imageUri?.toString(),
                            tipo = PaymentType.MENSUALIDAD
                        )
                        isLoading = false
                        submitted = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading && imageUri != null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar comprobante", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InstructionRow(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(20.dp), modifier = Modifier.size(24.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}

private fun getMonthName(month: Int) = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
).getOrNull(month - 1) ?: ""
