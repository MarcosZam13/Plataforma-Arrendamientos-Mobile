package com.plataforma.arrendamientos.ui.navigation

sealed class Screen(val route: String) {
    // ── Public ────────────────────────────────────────────────────────────────
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Registro : Screen("registro")
    object Propiedades : Screen("propiedades")
    object PropiedadDetalle : Screen("propiedades/{propiedadId}") {
        fun createRoute(id: String) = "propiedades/$id"
    }
    object AceptarInvitacion : Screen("invitacion/{token}") {
        fun createRoute(token: String) = "invitacion/$token"
    }
    object RecuperarContrasena : Screen("recuperar_contrasena")

    // ── Dashboard (role-based home) ────────────────────────────────────────
    object Dashboard : Screen("dashboard")

    // ── Dueño ─────────────────────────────────────────────────────────────
    object DuenoDashboard : Screen("dueno/dashboard")
    object MisPropiedades : Screen("dueno/propiedades")
    object NuevaPropiedad : Screen("dueno/propiedades/nueva")
    object EditarPropiedad : Screen("dueno/propiedades/{propiedadId}/editar") {
        fun createRoute(id: String) = "dueno/propiedades/$id/editar"
    }
    object Invitaciones : Screen("dueno/invitaciones")
    object NuevaInvitacion : Screen("dueno/invitaciones/nueva")
    object PagosRecibidos : Screen("dueno/pagos")
    object MensajesDueno : Screen("dueno/mensajes")
    object HistorialDueno : Screen("dueno/historial")
    object NotificacionesDueno : Screen("dueno/notificaciones")
    object PerfilDueno : Screen("dueno/perfil")

    // ── Inquilino ─────────────────────────────────────────────────────────
    object InquilinoDashboard : Screen("inquilino/dashboard")
    object MiContrato : Screen("inquilino/contrato")
    object SubirComprobante : Screen("inquilino/comprobante")
    object MensajesInquilino : Screen("inquilino/mensajes")
    object HistorialInquilino : Screen("inquilino/historial")
    object NotificacionesInquilino : Screen("inquilino/notificaciones")
    object PerfilInquilino : Screen("inquilino/perfil")
}
