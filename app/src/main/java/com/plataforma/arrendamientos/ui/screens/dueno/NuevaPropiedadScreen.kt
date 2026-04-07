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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaPropiedadScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var selectedMoneda by remember { mutableStateOf(Currency.USD) }
    var provincia by remember { mutableStateOf("San José") }
    var canton by remember { mutableStateOf("") }
    var distrito by remember { mutableStateOf("") }
    var selectedTipo by remember { mutableStateOf(PropertyType.APARTAMENTO) }
    var habitaciones by remember { mutableStateOf("") }
    var banos by remember { mutableStateOf("") }
    var parqueos by remember { mutableStateOf("") }
    var areaM2 by remember { mutableStateOf("") }
    var mascotas by remember { mutableStateOf(false) }
    var amueblado by remember { mutableStateOf(false) }
    var aguaIncluida by remember { mutableStateOf(false) }
    var luzIncluida by remember { mutableStateOf(false) }
    var internetIncluido by remember { mutableStateOf(false) }
    var seguridad by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva propiedad") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Basic info section
            SectionCard(title = "Información básica") {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la propiedad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio mensual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Moneda", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Currency.values().forEach { moneda ->
                                FilterChip(
                                    selected = selectedMoneda == moneda,
                                    onClick = { selectedMoneda = moneda },
                                    label = { Text(moneda.name) }
                                )
                            }
                        }
                    }
                }

                // Property type
                Text("Tipo de propiedad", style = MaterialTheme.typography.labelMedium)
                PropertyType.values().chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { tipo ->
                            FilterChip(
                                selected = selectedTipo == tipo,
                                onClick = { selectedTipo = tipo },
                                label = { Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            // Location section
            SectionCard(title = "Ubicación") {
                var provinciaExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = provinciaExpanded, onExpandedChange = { provinciaExpanded = it }) {
                    OutlinedTextField(
                        value = provincia,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Provincia") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(provinciaExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = provinciaExpanded, onDismissRequest = { provinciaExpanded = false }) {
                        listOf("San José", "Alajuela", "Cartago", "Heredia", "Guanacaste", "Puntarenas", "Limón").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { provincia = it; provinciaExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = canton, onValueChange = { canton = it },
                    label = { Text("Cantón") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = distrito, onValueChange = { distrito = it },
                    label = { Text("Distrito") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp)
                )
            }

            // Features section
            SectionCard(title = "Características") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = habitaciones, onValueChange = { habitaciones = it },
                        label = { Text("Habitaciones") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = banos, onValueChange = { banos = it },
                        label = { Text("Baños") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = parqueos, onValueChange = { parqueos = it },
                        label = { Text("Parqueos") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = areaM2, onValueChange = { areaM2 = it },
                        label = { Text("Área m²") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                }

                // Amenities toggles
                Text("Amenidades", style = MaterialTheme.typography.labelMedium)
                listOf(
                    Pair("Se aceptan mascotas") { v: Boolean -> mascotas = v } to mascotas,
                    Pair("Amueblado") { v: Boolean -> amueblado = v } to amueblado,
                    Pair("Agua incluida") { v: Boolean -> aguaIncluida = v } to aguaIncluida,
                    Pair("Luz incluida") { v: Boolean -> luzIncluida = v } to luzIncluida,
                    Pair("Internet incluido") { v: Boolean -> internetIncluido = v } to internetIncluido,
                    Pair("Seguridad") { v: Boolean -> seguridad = v } to seguridad,
                ).forEach { (pair, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pair.first, style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = value, onCheckedChange = pair.second)
                    }
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    propertyViewModel.addProperty(
                        Property(
                            id = "prop-${System.currentTimeMillis()}",
                            titulo = titulo,
                            descripcion = descripcion,
                            precio = precio.toDoubleOrNull() ?: 0.0,
                            moneda = selectedMoneda,
                            provincia = provincia,
                            canton = canton,
                            distrito = distrito,
                            tipo = selectedTipo,
                            estado = PropertyStatus.DISPONIBLE,
                            imagenes = listOf("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800"),
                            duenoId = user.id,
                            caracteristicas = PropertyFeatures(
                                habitaciones = habitaciones.toIntOrNull() ?: 0,
                                banos = banos.toIntOrNull() ?: 0,
                                parqueos = parqueos.toIntOrNull() ?: 0,
                                areaM2 = areaM2.toDoubleOrNull() ?: 0.0,
                                mascotas = mascotas,
                                amueblado = amueblado,
                                aguaIncluida = aguaIncluida,
                                luzIncluida = luzIncluida,
                                internetIncluido = internetIncluido,
                                seguridad = seguridad
                            )
                        )
                    )
                    isLoading = false
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading && titulo.isNotBlank() && precio.isNotBlank() && canton.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Crear propiedad", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
