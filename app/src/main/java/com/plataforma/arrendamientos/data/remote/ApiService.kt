package com.plataforma.arrendamientos.data.remote

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Properties ──────────────────────────────────────────────────────────

    @GET("propiedades")
    suspend fun getProperties(): Response<List<PropertyDto>>

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
    suspend fun createPayment(@Body body: CreatePaymentRequest): Response<PaymentResponseDto>

    @PUT("pagos/{id}")
    suspend fun updatePayment(
        @Path("id") id: String,
        @Body body: UpdatePaymentRequest
    ): Response<PaymentDto>

    // ─── Notifications ────────────────────────────────────────────────────────

    @GET("notificaciones/{userId}")
    suspend fun getNotificationsByUser(@Path("userId") userId: String): Response<List<NotificationDto>>

    @POST("notificaciones")
    suspend fun createNotification(@Body body: CreateNotificationRequest): Response<NotificationDto>

    @PUT("notificaciones/{id}")
    suspend fun updateNotification(
        @Path("id") id: String,
        @Body body: UpdateNotificationRequest
    ): Response<NotificationDto>

    // ─── Conversations ────────────────────────────────────────────────────────

    @GET("conversaciones/{userId}")
    suspend fun getConversationsByUser(@Path("userId") userId: String): Response<List<ConversationDto>>

    @POST("conversaciones")
    suspend fun createConversation(@Body body: CreateConversationRequest): Response<ConversationDto>

    // ─── Messages ─────────────────────────────────────────────────────────────

    @GET("mensajes/{userId}")
    suspend fun getMessagesByUser(@Path("userId") userId: String): Response<List<MessageDto>>

    @POST("mensajes")
    suspend fun createMessage(@Body body: CreateMessageRequest): Response<MessageDto>

    @PUT("mensajes/{id}")
    suspend fun updateMessage(
        @Path("id") id: String,
        @Body body: UpdateMessageRequest
    ): Response<MessageDto>
}
