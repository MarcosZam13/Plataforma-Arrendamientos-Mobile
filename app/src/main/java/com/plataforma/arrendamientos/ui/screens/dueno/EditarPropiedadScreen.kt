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
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPropiedadScreen(
    propiedadId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val property = propertyViewModel.getPropertyById(propiedadId)

    if (property == null) {
        Scaffold(topBar = { TopAppBar(title = { Text("Propiedad no encontrada") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Propiedad no encontrada")
            }
        }
        return
    }

    var titulo by remember { mutableStateOf(property.titulo) }
    var descripcion by remember { mutableStateOf(property.descripcion) }
    var precio by remember { mutableStateOf(property.precio.toString()) }
    var selectedMoneda by remember { mutableStateOf(property.moneda) }
    var canton by remember { mutableStateOf(property.canton) }
    var distrito by remember { mutableStateOf(property.distrito) }
    var selectedTipo by remember { mutableStateOf(property.tipo) }
    var selectedEstado by remember { mutableStateOf(property.estado) }
    var habitaciones by remember { mutableStateOf(property.caracteristicas.habitaciones.toString()) }
    var banos by remember { mutableStateOf(property.caracteristicas.banos.toString()) }
    var parqueos by remember { mutableStateOf(property.caracteristicas.parqueos.toString()) }
    var areaM2 by remember { mutableStateOf(property.caracteristicas.areaM2.toString()) }
    var mascotas by remember { mutableStateOf(property.caracteristicas.mascotas) }
    var amueblado by remember { mutableStateOf(property.caracteristicas.amueblado) }
    var aguaIncluida by remember { mutableStateOf(property.caracteristicas.aguaIncluida) }
    var luzIncluida by remember { mutableStateOf(property.caracteristicas.luzIncluida) }
    var internetIncluido by remember { mutableStateOf(property.caracteristicas.internetIncluido) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar propiedad") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Información básica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                    }

                    // Estado
                    Text("Estado", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PropertyStatus.values().forEach { estado ->
                            FilterChip(
                                selected = selectedEstado == estado,
                                onClick = { selectedEstado = estado },
                                label = { Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Características", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = habitaciones, onValueChange = { habitaciones = it }, label = { Text("Habitaciones") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                        OutlinedTextField(value = banos, onValueChange = { banos = it }, label = { Text("Baños") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                    }
                    listOf(
                        Pair("Mascotas") { v: Boolean -> mascotas = v } to mascotas,
                        Pair("Amueblado") { v: Boolean -> amueblado = v } to amueblado,
                        Pair("Agua incluida") { v: Boolean -> aguaIncluida = v } to aguaIncluida,
                        Pair("Luz incluida") { v: Boolean -> luzIncluida = v } to luzIncluida,
                        Pair("Internet") { v: Boolean -> internetIncluido = v } to internetIncluido,
                    ).forEach { (pair, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(pair.first, style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = value, onCheckedChange = pair.second)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    propertyViewModel.updateProperty(
                        property.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            precio = precio.toDoubleOrNull() ?: property.precio,
                            moneda = selectedMoneda,
                            canton = canton,
                            distrito = distrito,
                            tipo = selectedTipo,
                            estado = selectedEstado,
                            caracteristicas = property.caracteristicas.copy(
                                habitaciones = habitaciones.toIntOrNull() ?: 0,
                                banos = banos.toIntOrNull() ?: 0,
                                parqueos = parqueos.toIntOrNull() ?: 0,
                                areaM2 = areaM2.toDoubleOrNull() ?: 0.0,
                                mascotas = mascotas,
                                amueblado = amueblado,
                                aguaIncluida = aguaIncluida,
                                luzIncluida = luzIncluida,
                                internetIncluido = internetIncluido
                            )
                        )
                    )
                    isLoading = false
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading && titulo.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
