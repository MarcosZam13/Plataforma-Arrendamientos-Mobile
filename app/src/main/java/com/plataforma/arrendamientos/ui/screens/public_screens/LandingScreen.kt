package com.plataforma.arrendamientos.ui.screens.public_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plataforma.arrendamientos.ui.theme.Primary
import com.plataforma.arrendamientos.ui.theme.PrimaryContainer
import com.plataforma.arrendamientos.ui.theme.StatusGreen
import com.plataforma.arrendamientos.ui.theme.StatusGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onBrowseProperties: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ArrendaCR",
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                actions = {
                    TextButton(onClick = onLoginClick) {
                        Text("Iniciar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = PrimaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Plataforma de Arrendamientos Costa Rica",
                            color = Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = "Gestión de arrendamientos simple y segura",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "Conectamos propietarios e inquilinos en Costa Rica. Gestiona contratos, pagos y comunicación desde un solo lugar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onRegisterClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Comenzar gratis")
                        }
                        OutlinedButton(
                            onClick = onBrowseProperties,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver propiedades")
                        }
                    }
                }
            }

            // Features Section
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Todo lo que necesitas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                FeatureItem(
                    icon = Icons.Default.Home,
                    title = "Gestión de propiedades",
                    description = "Administra todas tus propiedades, fotos y estado desde un solo panel."
                )
                FeatureItem(
                    icon = Icons.Default.Description,
                    title = "Contratos digitales",
                    description = "Crea y firma contratos de arrendamiento de forma digital y segura."
                )
                FeatureItem(
                    icon = Icons.Default.Payment,
                    title = "Control de pagos",
                    description = "Recibe y aprueba comprobantes de pago SINPE o transferencia."
                )
                FeatureItem(
                    icon = Icons.Default.Message,
                    title = "Comunicación directa",
                    description = "Mensajería integrada entre propietarios e inquilinos."
                )
                FeatureItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    description = "Recibe alertas de pagos, mensajes y actualizaciones importantes."
                )
                FeatureItem(
                    icon = Icons.Default.Analytics,
                    title = "Historial y reportes",
                    description = "Accede al historial de pagos y descarga reportes en Excel."
                )
            }

            // CTA Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "¿Listo para empezar?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Regístrate gratis y empieza a gestionar tus arrendamientos hoy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRegisterClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Primary
                        )
                    ) {
                        Text("Crear cuenta gratis", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Footer
            Text(
                text = "© 2026 Plataforma de Arrendamientos Costa Rica",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = PrimaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
