package com.plataforma.arrendamientos.ui.screens.dueno

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.plataforma.arrendamientos.ui.components.EmptyState
import com.plataforma.arrendamientos.ui.components.PropertyCard
import com.plataforma.arrendamientos.viewmodel.AuthViewModel
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPropiedadesScreen(
    onAddProperty: () -> Unit,
    onEditProperty: (String) -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user ?: return

    val properties = propertyViewModel.getPropertiesByOwner(user.id)
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis propiedades") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProperty,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva propiedad") }
            )
        }
    ) { padding ->
        if (properties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.Home,
                    title = "Sin propiedades",
                    subtitle = "Agrega tu primera propiedad para empezar a arrendar.",
                    action = "Agregar propiedad",
                    onAction = onAddProperty
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${properties.size} propiedad${if (properties.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(properties, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onClick = {},
                        showActions = true,
                        onEdit = { onEditProperty(property.id) },
                        onDelete = { showDeleteDialog = property.id }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { propId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Eliminar propiedad") },
            text = { Text("¿Estás seguro de que deseas eliminar esta propiedad? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        propertyViewModel.deleteProperty(propId)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }
}
