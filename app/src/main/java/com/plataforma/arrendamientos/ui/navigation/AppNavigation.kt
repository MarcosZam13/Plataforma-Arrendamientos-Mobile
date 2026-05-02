package com.plataforma.arrendamientos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plataforma.arrendamientos.data.model.UserRole
import com.plataforma.arrendamientos.ui.screens.dueno.*
import com.plataforma.arrendamientos.ui.screens.inquilino.*
import com.plataforma.arrendamientos.ui.screens.public_screens.*
import com.plataforma.arrendamientos.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {
        // ── Public screens ─────────────────────────────────────────────────
        composable(Screen.Landing.route) {
            LandingScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Registro.route) },
                onBrowseProperties = { navController.navigate(Screen.Propiedades.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    val dest = if (user.rol == UserRole.DUENO) Screen.DuenoDashboard.route
                               else Screen.InquilinoDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Registro.route) },
                onForgotPassword = { navController.navigate(Screen.RecuperarContrasena.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegisterSuccess = { user ->
                    val dest = if (user.rol == UserRole.DUENO) Screen.DuenoDashboard.route
                               else Screen.InquilinoDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Propiedades.route) {
            PropiedadesScreen(
                onPropertyClick = { id ->
                    navController.navigate(Screen.PropiedadDetalle.createRoute(id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PropiedadDetalle.route,
            arguments = listOf(navArgument("propiedadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val propiedadId = backStackEntry.arguments?.getString("propiedadId") ?: ""
            PropiedadDetalleScreen(
                propiedadId = propiedadId,
                currentUser = authState.user,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.AceptarInvitacion.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            AceptarInvitacionScreen(
                token = token,
                currentUser = authState.user,
                onSuccess = {
                    navController.navigate(Screen.InquilinoDashboard.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.RecuperarContrasena.route) {
            RecuperarContrasenaScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Dueño screens ──────────────────────────────────────────────────
        composable(Screen.DuenoDashboard.route) {
            DuenoDashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.MisPropiedades.route) {
            MisPropiedadesScreen(
                onAddProperty = { navController.navigate(Screen.NuevaPropiedad.route) },
                onEditProperty = { id -> navController.navigate(Screen.EditarPropiedad.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NuevaPropiedad.route) {
            NuevaPropiedadScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditarPropiedad.route,
            arguments = listOf(navArgument("propiedadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val propiedadId = backStackEntry.arguments?.getString("propiedadId") ?: ""
            EditarPropiedadScreen(
                propiedadId = propiedadId,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Invitaciones.route) {
            InvitacionesScreen(
                onNewInvitation = { navController.navigate(Screen.NuevaInvitacion.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NuevaInvitacion.route) {
            NuevaInvitacionScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PagosRecibidos.route) {
            PagosRecibidosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MensajesDueno.route) {
            MensajesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HistorialDueno.route) {
            HistorialScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NotificacionesDueno.route) {
            NotificacionesScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.NotificacionesDueno.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.PerfilDueno.route) {
            PerfilScreen(
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                onNotificaciones = { navController.navigate(Screen.NotificacionesDueno.route) }
            )
        }

        // ── Inquilino screens ──────────────────────────────────────────────
        composable(Screen.InquilinoDashboard.route) {
            InquilinoDashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.MiContrato.route) {
            MiContratoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SubirComprobante.route) {
            SubirComprobanteScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MensajesInquilino.route) {
            MensajesInquilinoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HistorialInquilino.route) {
            HistorialInquilinoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NotificacionesInquilino.route) {
            NotificacionesInquilinoScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.NotificacionesInquilino.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.PerfilInquilino.route) {
            PerfilInquilinoScreen(
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
                onNotificaciones = { navController.navigate(Screen.NotificacionesInquilino.route) }
            )
        }
    }
}