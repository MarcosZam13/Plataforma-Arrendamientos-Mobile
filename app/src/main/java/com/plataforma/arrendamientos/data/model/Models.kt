package com.plataforma.arrendamientos.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

// ─── Enums ─────────────────────────────────────────────────────────────────

enum class UserRole { DUENO, INQUILINO }

enum class Currency { CRC, USD }

enum class PropertyType { CASA, APARTAMENTO, LOCAL, BODEGA, OFICINA }

enum class PropertyStatus { DISPONIBLE, ALQUILADA, MANTENIMIENTO }

enum class InvitationStatus { PENDIENTE, ACEPTADA, EXPIRADA, CANCELADA }

enum class PaymentType { MENSUALIDAD, DEPOSITO }

enum class PaymentStatus { PENDIENTE, APROBADO, RECHAZADO }

enum class ContractStatus { ACTIVO, FINALIZADO, CANCELADO }

enum class DepositStatus { PENDIENTE, PAGADO, DEVUELTO, RETENIDO }

enum class NotificationType {
    INVITACION_ENVIADA, INVITACION_ACEPTADA,
    PAGO_RECIBIDO, PAGO_APROBADO, PAGO_RECHAZADO,
    CONTRATO_ACTIVO, CONTRATO_FINALIZADO,
    MENSAJE_NUEVO
}

enum class MessageType { TEXT, IMAGE }

enum class MessageStatus { SENT, DELIVERED, READ }

// ─── Data Classes ──────────────────────────────────────────────────────────

data class User(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: UserRole,
    val avatar: String? = null,
    val telefono: String? = null,
    val createdAt: String = ""
)

data class Property(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    val moneda: Currency,
    val provincia: String,
    val canton: String,
    val distrito: String,
    val tipo: PropertyType,
    val estado: PropertyStatus,
    val imagenes: List<String>,
    val duenoId: String,
    val caracteristicas: PropertyFeatures,
    val createdAt: String = ""
)

data class PropertyFeatures(
    val habitaciones: Int = 0,
    val banos: Int = 0,
    val parqueos: Int = 0,
    val areaM2: Double = 0.0,
    val mascotas: Boolean = false,
    val amueblado: Boolean = false,
    val aguaIncluida: Boolean = false,
    val luzIncluida: Boolean = false,
    val internetIncluido: Boolean = false,
    val seguridad: Boolean = false,
    val piscina: Boolean = false,
    val gimnasio: Boolean = false
)

data class Invitation(
    val id: String,
    val token: String,
    val propiedadId: String,
    val duenoId: String,
    val inquilinoCorreo: String? = null,
    val inquilinoId: String? = null,
    val estado: InvitationStatus,
    val fechaEmision: String,
    val fechaExpiracion: String,
    val montoAlquiler: Double,
    val montoDeposito: Double,
    val moneda: Currency,
    val notas: String? = null
)

data class Payment(
    val id: String,
    val tipo: PaymentType,
    val contratoId: String,
    val propiedadId: String,
    val inquilinoId: String,
    val duenoId: String,
    val mes: Int,
    val anio: Int,
    val monto: Double,
    val moneda: Currency,
    val comprobante: String? = null, // base64 or URL
    val estado: PaymentStatus,
    val fechaSubida: String? = null,
    val fechaRevision: String? = null,
    val motivoRechazo: String? = null
)

data class Contract(
    val id: String,
    val invitacionId: String,
    val propiedadId: String,
    val duenoId: String,
    val inquilinoId: String,
    val montoMensual: Double,
    val montoDeposito: Double,
    val moneda: Currency,
    val fechaInicio: String,
    val estado: ContractStatus,
    val estadoDeposito: DepositStatus
)

data class AppNotification(
    val id: String,
    val userId: String,
    val tipo: NotificationType,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean,
    val fecha: String,
    val link: String? = null
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val type: MessageType,
    val status: MessageStatus,
    val timestamp: String
)

data class Conversation(
    val id: String,
    val participants: List<String>,
    val propertyId: String? = null,
    val lastMessage: String? = null,
    val lastMessageAt: String? = null,
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: String = "",
    val otherUserName: String = "",
    val otherUserAvatar: String? = null,
    val propertyTitle: String? = null
)

// ─── UI State Helpers ──────────────────────────────────────────────────────

data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
