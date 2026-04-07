package com.plataforma.arrendamientos.ui.screens.public_screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.plataforma.arrendamientos.data.model.Currency
import com.plataforma.arrendamientos.data.model.User
import com.plataforma.arrendamientos.ui.components.PropertyStatusBadge
import com.plataforma.arrendamientos.ui.components.formatPrice
import com.plataforma.arrendamientos.viewmodel.PropertyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropiedadDetalleScreen(
    propiedadId: String,
    currentUser: User?,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    propertyViewModel: PropertyViewModel = hiltViewModel()
) {
    val property = propertyViewModel.getPropertyById(propiedadId)

    if (property == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Propiedad no encontrada") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Propiedad no encontrada", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property.titulo, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatPrice(property.precio, property.moneda),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "por mes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = if (currentUser != null) { {} } else onLogin,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(if (currentUser != null) "Contactar" else "Iniciar sesión")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Image
            AsyncImage(
                model = property.imagenes.firstOrNull(),
                contentDescription = property.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = property.tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    PropertyStatusBadge(property.estado)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = property.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${property.distrito}, ${property.canton}, ${property.provincia}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Features grid
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Características",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        val c = property.caracteristicas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            if (c.habitaciones > 0) FeatureDetail(Icons.Default.Bed, "${c.habitaciones}", "Habitaciones")
                            if (c.banos > 0) FeatureDetail(Icons.Default.Bathroom, "${c.banos}", "Baños")
                            if (c.parqueos > 0) FeatureDetail(Icons.Default.DirectionsCar, "${c.parqueos}", "Parqueos")
                            if (c.areaM2 > 0) FeatureDetail(Icons.Default.SquareFoot, "${c.areaM2.toInt()} m²", "Área")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Amenities
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Amenidades", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        val c = property.caracteristicas
                        val amenities = buildList {
                            if (c.mascotas) add(Pair(Icons.Default.Pets, "Se aceptan mascotas"))
                            if (c.amueblado) add(Pair(Icons.Default.Chair, "Amueblado"))
                            if (c.aguaIncluida) add(Pair(Icons.Default.Water, "Agua incluida"))
                            if (c.luzIncluida) add(Pair(Icons.Default.ElectricBolt, "Luz incluida"))
                            if (c.internetIncluido) add(Pair(Icons.Default.Wifi, "Internet incluido"))
                            if (c.seguridad) add(Pair(Icons.Default.Security, "Seguridad"))
                            if (c.piscina) add(Pair(Icons.Default.Pool, "Piscina"))
                            if (c.gimnasio) add(Pair(Icons.Default.FitnessCenter, "Gimnasio"))
                        }
                        if (amenities.isEmpty()) {
                            Text("Sin amenidades adicionales", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            amenities.chunked(2).forEach { row ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    row.forEach { (icon, label) ->
                                        Row(
                                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                            Text(label, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Description
                Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(property.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun FeatureDetail(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
