package com.plataforma.arrendamientos.ui.screens.dueno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.data.model.Currency
import com.plataforma.arrendamientos.ui.theme.StatusGreen
import com.plataforma.arrendamientos.ui.theme.StatusGreenContainer
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.InvitationViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaInvitacionScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    invitationViewModel: InvitationViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return
    val clipboard = LocalClipboardManager.current

    val myProperties = propertyViewModel.getPropertiesByOwner(user.id)
    var selectedPropertyId by remember { mutableStateOf(myProperties.firstOrNull()?.id ?: "") }
    var inquilinoCorreo by remember { mutableStateOf("") }
    var montoAlquiler by remember { mutableStateOf("") }
    var montoDeposito by remember { mutableStateOf("") }
    var selectedMoneda by remember { mutableStateOf(Currency.USD) }
    var notas by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var createdToken by remember { mutableStateOf<String?>(null) }
    var copiedToClipboard by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva invitación") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            if (createdToken != null) {
                // Success state
                Card(colors = CardDefaults.cardColors(containerColor = StatusGreenContainer), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("¡Invitación creada!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusGreen)
                        Text("Comparte el siguiente enlace con el inquilino:", style = MaterialTheme.typography.bodySmall, color = StatusGreen)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = "arrendamientos://invitacion/$createdToken",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    clipboard.setText(AnnotatedString("arrendamientos://invitacion/$createdToken"))
                                    copiedToClipboard = true
                                }) {
                                    Icon(if (copiedToClipboard) Icons.Default.Check else Icons.Default.ContentCopy, null)
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth()) {
                            Text("Ver invitaciones")
                        }
                    }
                }
            } else {
                // Form
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Datos de la invitación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                        // Property selector
                        var propExpanded by remember { mutableStateOf(false) }
                        val selectedProperty = myProperties.find { it.id == selectedPropertyId }
                        ExposedDropdownMenuBox(expanded = propExpanded, onExpandedChange = { propExpanded = it }) {
                            OutlinedTextField(
                                value = selectedProperty?.titulo ?: "Seleccionar propiedad",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Propiedad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(propExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(expanded = propExpanded, onDismissRequest = { propExpanded = false }) {
                                if (myProperties.isEmpty()) {
                                    DropdownMenuItem(text = { Text("No tienes propiedades") }, onClick = { propExpanded = false })
                                } else {
                                    myProperties.forEach { prop ->
                                        DropdownMenuItem(
                                            text = { Text(prop.titulo) },
                                            onClick = { selectedPropertyId = prop.id; propExpanded = false }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = inquilinoCorreo, onValueChange = { inquilinoCorreo = it },
                            label = { Text("Correo del inquilino") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = montoAlquiler, onValueChange = { montoAlquiler = it },
                                label = { Text("Monto mensual") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = montoDeposito, onValueChange = { montoDeposito = it },
                                label = { Text("Depósito") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Text("Moneda", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Currency.values().forEach { moneda ->
                                FilterChip(selected = selectedMoneda == moneda, onClick = { selectedMoneda = moneda }, label = { Text(moneda.name) })
                            }
                        }

                        OutlinedTextField(
                            value = notas, onValueChange = { notas = it },
                            label = { Text("Notas (opcional)") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        isLoading = true
                        val inv = invitationViewModel.createInvitation(
                            propiedadId = selectedPropertyId,
                            duenoId = user.id,
                            inquilinoCorreo = inquilinoCorreo,
                            montoAlquiler = montoAlquiler.toDoubleOrNull() ?: 0.0,
                            montoDeposito = montoDeposito.toDoubleOrNull() ?: 0.0,
                            moneda = selectedMoneda,
                            notas = notas
                        )
                        createdToken = inv.token
                        isLoading = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading && selectedPropertyId.isNotBlank() && inquilinoCorreo.isNotBlank() && montoAlquiler.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Crear invitación", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
