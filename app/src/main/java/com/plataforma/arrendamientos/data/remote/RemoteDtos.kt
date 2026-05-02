package com.plataforma.arrendamientos.data.remote

import com.plataforma.arrendamientos.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Response DTOs ────────────────────────────────────────────────────────────

@Serializable
data class PropertyDto(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val moneda: String = "CRC",
    val provincia: String = "",
    val canton: String = "",
    val distrito: String = "",
    val tipo: String = "apartamento",
    val estado: String = "disponible",
    val imagenes: List<String> = emptyList(),
    val duenoId: String? = null,
    val idDueno: String? = null,
    val caracteristicas: List<String>? = null,
    val amenidades: List<String>? = null,
    val habitaciones: Int? = null,
    val banos: Int? = null,
    val area: Int? = null,
    val fechaCreacion: String? = null
) {
    fun toDomain(): Property? {
        if (id.isBlank()) return null
        val amenList = amenidades ?: caracteristicas ?: emptyList()
        val lower = amenList.map { it.lowercase() }
        return Property(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            precio = precio,
            moneda = if (moneda.uppercase() == "USD") Currency.USD else Currency.CRC,
            provincia = provincia,
            canton = canton,
            distrito = distrito,
            tipo = when (tipo.lowercase()) {
                "casa" -> PropertyType.CASA
                "local" -> PropertyType.LOCAL
                "bodega" -> PropertyType.BODEGA
                "oficina" -> PropertyType.OFICINA
                else -> PropertyType.APARTAMENTO
            },
            estado = when (estado.lowercase()) {
                "alquilado", "alquilada" -> PropertyStatus.ALQUILADA
                "mantenimiento" -> PropertyStatus.MANTENIMIENTO
                else -> PropertyStatus.DISPONIBLE
            },
            imagenes = imagenes,
            duenoId = duenoId ?: idDueno ?: "",
            caracteristicas = PropertyFeatures(
                habitaciones = habitaciones ?: 0,
                banos = banos ?: 0,
                areaM2 = area?.toDouble() ?: 0.0,
                parqueos = if (lower.any { "parqueo" in it }) 1 else 0,
                amueblado = lower.any { "muebl" in it },
                aguaIncluida = lower.any { "agua" in it },
                luzIncluida = lower.any { "luz" in it || "electric" in it },
                internetIncluido = lower.any { "internet" in it || "wifi" in it },
                seguridad = lower.any { "seguridad" in it },
                piscina = lower.any { "piscina" in it },
                gimnasio = lower.any { "gimnasio" in it },
                mascotas = lower.any { "mascota" in it }
            ),
            createdAt = fechaCreacion ?: ""
        )
    }
}

@Serializable
data class UserDto(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String? = null,
    val rol: String = "arrendatario",
    val fechaRegistro: String? = null,
    val propiedades: List<String>? = null
) {
    fun toDomain(): User? {
        if (id.isBlank()) return null
        return User(
            id = id,
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            rol = when (rol.lowercase()) {
                "arrendador", "dueno", "dueño" -> UserRole.DUENO
                else -> UserRole.INQUILINO
            }
        )
    }
}

@Serializable
data class ContractDto(
    val id: String = "",
    val propiedadId: String? = null,
    val idPropiedad: String? = null,
    val arrendatarioId: String? = null,
    val idInquilino: String? = null,
    val arrendadorId: String? = null,
    val idDueno: String? = null,
    val estado: String = "activo",
    val monto: Double? = null,
    val montoMensual: Double? = null,
    val deposito: Double? = null,
    val moneda: String = "CRC",
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val terminos: String? = null,
    val fechaCreacion: String? = null
) {
    fun toDomain(): Contract? {
        if (id.isBlank()) return null
        return Contract(
            id = id,
            invitacionId = "",
            propiedadId = propiedadId ?: idPropiedad ?: "",
            inquilinoId = arrendatarioId ?: idInquilino ?: "",
            duenoId = arrendadorId ?: idDueno ?: "",
            montoMensual = montoMensual ?: monto ?: 0.0,
            montoDeposito = deposito ?: 0.0,
            moneda = if (moneda.uppercase() == "USD") Currency.USD else Currency.CRC,
            fechaInicio = fechaInicio ?: "",
            estado = when (estado.lowercase()) {
                "finalizado" -> ContractStatus.FINALIZADO
                "cancelado" -> ContractStatus.CANCELADO
                else -> ContractStatus.ACTIVO
            },
            estadoDeposito = DepositStatus.PAGADO
        )
    }
}

@Serializable
data class InvitationDto(
    val id: String = "",
    val idPropiedad: String? = null,
    val propiedadId: String? = null,
    val correoInvitado: String? = null,
    val inquilinoCorreo: String? = null,
    val token: String = "",
    val estado: String = "pendiente",
    val fechaEnvio: String? = null,
    val mensaje: String? = null,
    val monto: Double? = null,
    val montoAlquiler: Double? = null,
    val deposito: Double? = null,
    val montoDeposito: Double? = null
) {
    fun toDomain(): Invitation? {
        if (id.isBlank()) return null
        return Invitation(
            id = id,
            token = token.ifBlank { id },
            propiedadId = propiedadId ?: idPropiedad ?: "",
            duenoId = "",
            inquilinoCorreo = correoInvitado ?: inquilinoCorreo,
            estado = when (estado.lowercase()) {
                "aceptada" -> InvitationStatus.ACEPTADA
                "expirada" -> InvitationStatus.EXPIRADA
                "cancelada", "rechazada" -> InvitationStatus.CANCELADA
                else -> InvitationStatus.PENDIENTE
            },
            fechaEmision = fechaEnvio ?: "",
            fechaExpiracion = "",
            montoAlquiler = montoAlquiler ?: monto ?: 0.0,
            montoDeposito = deposito ?: montoDeposito ?: 0.0,
            moneda = Currency.CRC,
            notas = mensaje
        )
    }
}

@Serializable
data class PaymentDto(
    val id: String = "",
    val tipo: String = "mensualidad",
    val idContrato: String? = null,
    val contratoId: String? = null,
    val idPropiedad: String? = null,
    val propiedadId: String? = null,
    val idInquilino: String? = null,
    val inquilinoId: String? = null,
    val idDueno: String? = null,
    val duenoId: String? = null,
    val mes: Int = 0,
    @SerialName("año")
    val anio: Int? = null,
    val monto: Double = 0.0,
    val moneda: String = "CRC",
    val estado: String = "pendiente",
    val fechaSubida: String? = null,
    val fechaRevision: String? = null,
    val motivoRechazo: String? = null
) {
    fun toDomain(): Payment? {
        if (id.isBlank()) return null
        return Payment(
            id = id,
            tipo = if (tipo.lowercase() == "deposito") PaymentType.DEPOSITO else PaymentType.MENSUALIDAD,
            contratoId = contratoId ?: idContrato ?: "",
            propiedadId = propiedadId ?: idPropiedad ?: "",
            inquilinoId = inquilinoId ?: idInquilino ?: "",
            duenoId = duenoId ?: idDueno ?: "",
            mes = mes,
            anio = anio ?: 2026,
            monto = monto,
            moneda = if (moneda.uppercase() == "USD") Currency.USD else Currency.CRC,
            estado = when (estado.lowercase()) {
                "aprobado" -> PaymentStatus.APROBADO
                "rechazado" -> PaymentStatus.RECHAZADO
                else -> PaymentStatus.PENDIENTE
            },
            fechaSubida = fechaSubida,
            fechaRevision = fechaRevision,
            motivoRechazo = motivoRechazo
        )
    }
}

@Serializable
data class NotificationDto(
    val id: String = "",
    val userId: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "mensaje_nuevo",
    val leida: Boolean = false,
    val fecha: String = "",
    val link: String? = null
) {
    fun toDomain(): AppNotification? {
        if (id.isBlank()) return null
        return AppNotification(
            id = id,
            userId = userId,
            tipo = when (tipo.lowercase()) {
                "invitacion_enviada" -> NotificationType.INVITACION_ENVIADA
                "invitacion_aceptada" -> NotificationType.INVITACION_ACEPTADA
                "pago_recibido" -> NotificationType.PAGO_RECIBIDO
                "pago_aprobado" -> NotificationType.PAGO_APROBADO
                "pago_rechazado" -> NotificationType.PAGO_RECHAZADO
                "contrato_activo" -> NotificationType.CONTRATO_ACTIVO
                "contrato_finalizado", "contrato_proximo_vencer" -> NotificationType.CONTRATO_FINALIZADO
                else -> NotificationType.MENSAJE_NUEVO
            },
            titulo = titulo,
            mensaje = mensaje,
            leida = leida,
            fecha = fecha,
            link = link
        )
    }
}

@Serializable
data class ConversationDto(
    val id: String = "",
    val propertyId: String? = null,
    val participants: List<String> = emptyList(),
    val type: String? = null,
    val lastMessage: String? = null,
    val lastMessageAt: String? = null,
    val createdAt: String? = null,
    val unreadCount: Map<String, Int> = emptyMap()
) {
    fun toDomain(): Conversation? {
        if (id.isBlank()) return null
        return Conversation(
            id = id,
            participants = participants,
            propertyId = propertyId,
            lastMessage = lastMessage,
            lastMessageAt = lastMessageAt,
            unreadCount = unreadCount,
            createdAt = createdAt ?: ""
        )
    }
}

@Serializable
data class MessageDto(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: String = "text",
    val status: String = "sent",
    val timestamp: String = ""
) {
    fun toDomain(): Message? {
        if (id.isBlank()) return null
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            type = if (type == "image") MessageType.IMAGE else MessageType.TEXT,
            status = when (status.lowercase()) {
                "delivered" -> MessageStatus.DELIVERED
                "read" -> MessageStatus.READ
                else -> MessageStatus.SENT
            },
            timestamp = timestamp
        )
    }
}

@Serializable
data class PaymentResponseDto(
    val success: Boolean = false,
    val message: String = ""
)

// ─── Request Bodies ───────────────────────────────────────────────────────────

@Serializable
data class CreatePropertyRequest(
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    val moneda: String = "CRC",
    val provincia: String,
    val canton: String,
    val distrito: String,
    val tipo: String,
    val idDueno: String,
    val amenidades: List<String> = emptyList(),
    val imagenes: List<String> = emptyList()
)

@Serializable
data class UpdatePropertyRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    val estado: String? = null
)

@Serializable
data class CreateInvitationRequest(
    val idPropiedad: String,
    val correoInvitado: String,
    val token: String,
    val estado: String = "pendiente",
    val mensaje: String? = null,
    val monto: Double = 0.0,
    val deposito: Double = 0.0
)

@Serializable
data class UpdateInvitationRequest(val estado: String)

@Serializable
data class CreateContractRequest(
    val idPropiedad: String,
    val idInquilino: String,
    val idDueno: String,
    val montoMensual: Double,
    val deposito: Double,
    val moneda: String = "CRC",
    val fechaInicio: String,
    val idInvitacion: String? = null
)

@Serializable
data class CreatePaymentRequest(
    val tipo: String,
    val idContrato: String,
    val idPropiedad: String,
    val idInquilino: String,
    val idDueno: String,
    val mes: Int,
    @SerialName("año")
    val anio: Int,
    val monto: Double,
    val moneda: String = "CRC",
    val comprobante: String? = null
)

@Serializable
data class UpdatePaymentRequest(
    val estado: String,
    val motivoRechazo: String? = null
)

@Serializable
data class CreateNotificationRequest(
    val userId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val link: String? = null
)

@Serializable
data class UpdateNotificationRequest(val leida: Boolean)

@Serializable
data class CreateConversationRequest(
    val participants: List<String>,
    val propertyId: String? = null,
    val type: String = "general"
)

@Serializable
data class CreateMessageRequest(
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val type: String = "text"
)

@Serializable
data class UpdateMessageRequest(val status: String)

@Serializable
data class CreateUserRequest(
    val nombre: String,
    val correo: String,
    val telefono: String? = null,
    val rol: String
)

// ─── Domain → Request mappers ─────────────────────────────────────────────────

fun Property.toCreateRequest() = CreatePropertyRequest(
    titulo = titulo,
    descripcion = descripcion,
    precio = precio,
    moneda = moneda.name,
    provincia = provincia,
    canton = canton,
    distrito = distrito,
    tipo = tipo.name.lowercase(),
    idDueno = duenoId,
    imagenes = imagenes
)

fun Invitation.toCreateRequest() = CreateInvitationRequest(
    idPropiedad = propiedadId,
    correoInvitado = inquilinoCorreo ?: "",
    token = token,
    monto = montoAlquiler,
    deposito = montoDeposito,
    mensaje = notas
)

fun Contract.toCreateRequest() = CreateContractRequest(
    idPropiedad = propiedadId,
    idInquilino = inquilinoId,
    idDueno = duenoId,
    montoMensual = montoMensual,
    deposito = montoDeposito,
    moneda = moneda.name,
    fechaInicio = fechaInicio,
    idInvitacion = invitacionId.ifBlank { null }
)

fun Payment.toCreateRequest() = CreatePaymentRequest(
    tipo = tipo.name.lowercase(),
    idContrato = contratoId,
    idPropiedad = propiedadId,
    idInquilino = inquilinoId,
    idDueno = duenoId,
    mes = mes,
    anio = anio,
    monto = monto,
    moneda = moneda.name,
    comprobante = comprobante
)

fun AppNotification.toCreateRequest() = CreateNotificationRequest(
    userId = userId,
    titulo = titulo,
    mensaje = mensaje,
    tipo = tipo.name.lowercase(),
    link = link
)
