package com.plataforma.arrendamientos.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/registro")
    suspend fun register(@Body body: RegisterRequest): Response<LoginResponse>

    // ─── Properties ───────────────────────────────────────────────────────────

    @GET("propiedades")
    suspend fun getProperties(
        @Query("limit") limit: Int = 100
    ): Response<PropiedadListResponse>

    @GET("propiedades/{id}")
    suspend fun getProperty(@Path("id") id: String): Response<PropertyDto>

    @POST("propiedades")
    suspend fun createProperty(@Body body: CreatePropertyRequest): Response<PropertyDto>

    @PUT("propiedades/{id}")
    suspend fun updateProperty(
        @Path("id") id: String,
        @Body body: UpdatePropertyRequest
    ): Response<PropertyDto>

    @DELETE("propiedades/{id}")
    suspend fun deleteProperty(@Path("id") id: String): Response<Unit>

    // ─── Users ────────────────────────────────────────────────────────────────

    @GET("usuarios")
    suspend fun getUsers(): Response<List<UserDto>>

    @GET("usuarios/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserDto>

    @POST("usuarios")
    suspend fun createUser(@Body body: CreateUserRequest): Response<UserDto>

    // ─── Contracts ────────────────────────────────────────────────────────────

    @GET("contratos")
    suspend fun getContracts(): Response<List<ContractDto>>

    @GET("contratos/{id}")
    suspend fun getContract(@Path("id") id: String): Response<ContractDto>

    @POST("contratos")
    suspend fun createContract(@Body body: CreateContractRequest): Response<ContractDto>

    // ─── Invitations ──────────────────────────────────────────────────────────

    @GET("invitaciones")
    suspend fun getInvitations(): Response<List<InvitationDto>>

    @GET("invitaciones/{id}")
    suspend fun getInvitation(@Path("id") id: String): Response<InvitationDto>

    @POST("invitaciones")
    suspend fun createInvitation(@Body body: CreateInvitationRequest): Response<InvitationDto>

    @PUT("invitaciones/{id}")
    suspend fun updateInvitation(
        @Path("id") id: String,
        @Body body: UpdateInvitationRequest
    ): Response<InvitationDto>

    @DELETE("invitaciones/{id}")
    suspend fun deleteInvitation(@Path("id") id: String): Response<Unit>

    // ─── Payments ─────────────────────────────────────────────────────────────

    @GET("pagos/{userId}")
    suspend fun getPaymentsByUser(@Path("userId") userId: String): Response<List<PaymentDto>>

    @POST("pagos")
    suspend fun createPayment(@Body body: CreatePaymentRequest): Response<PaymentDto>

    @PUT("pagos/{id}")
    suspend fun updatePayment(
        @Path("id") id: String,
        @Body body: UpdatePaymentRequest
    ): Response<PaymentDto>

    // ─── MS Notificaciones ────────────────────────────────────────────────────

    @GET("notificaciones/{userId}")
    suspend fun getNotificacionesByUser(@Path("userId") userId: String): Response<MsNotificacionesResponse>

    @PATCH("notificaciones/{id}/leer")
    suspend fun marcarNotificacionLeida(
        @Path("id") id: String
    ): Response<Unit>

    // ─── MS Mensajes ──────────────────────────────────────────────────────────

    @GET("mensajes/conversaciones")
    suspend fun getConversacionesByUser(): Response<MsMensajesConversacionesResponse>

    @GET("mensajes/conversaciones/{conversationId}/mensajes")
    suspend fun getHistorialMensajes(@Path("conversationId") conversationId: String): Response<MsMensajesHistorialResponse>

    @POST("mensajes")
    suspend fun enviarMensaje(@Body body: MsMensajesEnviarRequest): Response<MsMensajesEnviarResponse>
}
