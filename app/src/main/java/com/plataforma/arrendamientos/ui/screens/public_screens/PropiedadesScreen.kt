package com.plataforma.arrendamientos.ui.screens.public_screens

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.data.model.MockData
import com.plataforma.arrendamientos.data.model.PropertyType
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.ui.components.PropertyCard
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropiedadesScreen(
    onPropertyClick: (String) -> Unit,
    onBack: () -> Unit,
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProvincia by remember { mutableStateOf<String?>(null) }
    var selectedTipo by remember { mutableStateOf<PropertyType?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    val filteredProperties = propertyViewModel.searchProperties(
        query = searchQuery,
        provincia = selectedProvincia,
        tipo = selectedTipo,
        maxPrecio = null
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Propiedades disponibles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filtros"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar propiedades...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filters panel
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Province filter
                        var provinciaExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = provinciaExpanded,
                            onExpandedChange = { provinciaExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedProvincia ?: "Todas las provincias",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Provincia") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinciaExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = provinciaExpanded,
                                onDismissRequest = { provinciaExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas las provincias") },
                                    onClick = { selectedProvincia = null; provinciaExpanded = false }
                                )
                                MockData.PROVINCES.forEach { provincia ->
                                    DropdownMenuItem(
                                        text = { Text(provincia) },
                                        onClick = { selectedProvincia = provincia; provinciaExpanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Type filter chips
                        Text("Tipo de propiedad", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PropertyType.values().take(3).forEach { tipo ->
                                FilterChip(
                                    selected = selectedTipo == tipo,
                                    onClick = {
                                        selectedTipo = if (selectedTipo == tipo) null else tipo
                                    },
                                    label = { Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PropertyType.values().drop(3).forEach { tipo ->
                                FilterChip(
                                    selected = selectedTipo == tipo,
                                    onClick = {
                                        selectedTipo = if (selectedTipo == tipo) null else tipo
                                    },
                                    label = { Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                selectedProvincia = null
                                selectedTipo = null
                            }
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }

            // Results count
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProperties.size} propiedad${if (filteredProperties.size != 1) "es" else ""} encontrada${if (filteredProperties.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Property list
            if (filteredProperties.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "Sin resultados",
                        subtitle = "No se encontraron propiedades con esos filtros.",
                        action = "Limpiar filtros",
                        onAction = {
                            searchQuery = ""
                            selectedProvincia = null
                            selectedTipo = null
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProperties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onClick = { onPropertyClick(property.id) }
                        )
                    }
                }
            }
        }
    }
}
