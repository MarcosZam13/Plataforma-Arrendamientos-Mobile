package com.plataforma.arrendamientos.ui.screens.dueno

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.ui.theme.StatusRed
import com.plataforma.arrendamientos.ui.theme.StatusRedContainer
import com.plataforma.arrendamientos.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNotificaciones: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf(user.nombre) }
    var telefono by remember { mutableStateOf("") }
    var editMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { editMode = !editMode }) {
                        Icon(if (editMode) Icons.Default.Close else Icons.Default.Edit, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Avatar section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.nombre.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(user.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(user.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (user.rol.name == "DUENO") "Propietario" else "Inquilino",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Profile info card
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Información personal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                    if (editMode) {
                        OutlinedTextField(
                            value = nombre, onValueChange = { nombre = it },
                            label = { Text("Nombre completo") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = telefono, onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = { editMode = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Guardar cambios") }
                    } else {
                        ProfileInfoRow(Icons.Default.Person, "Nombre", user.nombre)
                        ProfileInfoRow(Icons.Default.Email, "Correo", user.correo)
                        if (telefono.isNotBlank()) ProfileInfoRow(Icons.Default.Phone, "Teléfono", telefono)
                    }
                }
            }

            // Settings card
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Configuración", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))

                    ListItem(
                        headlineContent = { Text("Notificaciones") },
                        supportingContent = { Text("Gestionar alertas y notificaciones") },
                        leadingContent = { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier = Modifier.clickable { onNotificaciones() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    ListItem(
                        headlineContent = { Text("Seguridad") },
                        supportingContent = { Text("Cambiar contraseña") },
                        leadingContent = { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier = Modifier.clickable { showChangePasswordDialog = true }
                    )
                }
            }

            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = StatusRed)) { Text("Cerrar sesión") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showChangePasswordDialog) {
        CambiarContrasenaDialog(onDismiss = { showChangePasswordDialog = false })
    }
}

@Composable
private fun CambiarContrasenaDialog(onDismiss: () -> Unit) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, null) },
        title = { Text(if (success) "Contraseña actualizada" else "Cambiar contraseña") },
        text = {
            if (success) {
                Text("Tu contraseña ha sido actualizada correctamente.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = actual,
                        onValueChange = { actual = it; error = null },
                        label = { Text("Contraseña actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = nueva,
                        onValueChange = { nueva = it; error = null },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = confirmar,
                        onValueChange = { confirmar = it; error = null },
                        label = { Text("Confirmar nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (success) {
                Button(onClick = onDismiss) { Text("Listo") }
            } else {
                Button(onClick = {
                    error = when {
                        actual != "123456" -> "La contraseña actual es incorrecta"
                        nueva.length < 6 -> "La nueva contraseña debe tener al menos 6 caracteres"
                        nueva != confirmar -> "Las contraseñas no coinciden"
                        else -> null
                    }
                    if (error == null) success = true
                }) { Text("Guardar") }
            }
        },
        dismissButton = {
            if (!success) {
                OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}

@Composable
fun PerfilInquilinoScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onNotificaciones: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    PerfilScreen(onLogout = onLogout, onBack = onBack, onNotificaciones = onNotificaciones, authViewModel = authViewModel)
}

@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}