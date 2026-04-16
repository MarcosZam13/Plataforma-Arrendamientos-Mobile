package com.plataforma.arrendamientos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.plataforma.arrendamientos.data.model.*
import com.plataforma.arrendamientos.ui.theme.*

// ─── Status Badge ─────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PropertyStatusBadge(status: PropertyStatus) {
    val (text, color, container) = when (status) {
        PropertyStatus.DISPONIBLE -> Triple("Disponible", StatusGreen, StatusGreenContainer)
        PropertyStatus.ALQUILADA -> Triple("Alquilada", StatusBlue, StatusBlueContainer)
        PropertyStatus.MANTENIMIENTO -> Triple("Mantenimiento", StatusAmber, StatusAmberContainer)
    }
    StatusBadge(text = text, color = color, containerColor = container)
}

@Composable
fun PaymentStatusBadge(status: PaymentStatus) {
    val (text, color, container) = when (status) {
        PaymentStatus.PENDIENTE -> Triple("Pendiente", StatusAmber, StatusAmberContainer)
        PaymentStatus.APROBADO -> Triple("Aprobado", StatusGreen, StatusGreenContainer)
        PaymentStatus.RECHAZADO -> Triple("Rechazado", StatusRed, StatusRedContainer)
    }
    StatusBadge(text = text, color = color, containerColor = container)
}

// ─── Property Card ────────────────────────────────────────────────────────────

@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showActions: Boolean = false,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Property image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                AsyncImage(
                    model = property.imagenes.firstOrNull(),
                    contentDescription = property.titulo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Status badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    PropertyStatusBadge(property.estado)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Type badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = property.tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = property.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${property.canton}, ${property.provincia}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Features row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (property.caracteristicas.habitaciones > 0) {
                        FeatureChip(
                            icon = Icons.Default.Bed,
                            text = "${property.caracteristicas.habitaciones}"
                        )
                    }
                    if (property.caracteristicas.banos > 0) {
                        FeatureChip(
                            icon = Icons.Default.Bathroom,
                            text = "${property.caracteristicas.banos}"
                        )
                    }
                    if (property.caracteristicas.areaM2 > 0) {
                        FeatureChip(
                            icon = Icons.Default.SquareFoot,
                            text = "${property.caracteristicas.areaM2.toInt()} m²"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatPrice(property.precio, property.moneda),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "por mes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showActions && onEdit != null && onDelete != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = StatusRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Stat Card ────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = iconContainerColor,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(text = action, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (action != null && onAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAction) { Text(action) }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

fun formatPrice(price: Double, currency: Currency): String {
    return when (currency) {
        Currency.USD -> "$ ${String.format("%,.0f", price)}"
        Currency.CRC -> "₡ ${String.format("%,.0f", price)}"
    }
}

val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
