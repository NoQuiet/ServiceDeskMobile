package com.example.servicedeskmobile.data.api

import com.example.servicedeskmobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Map<String, Any>>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<Map<String, String>>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    @GET("tickets")
    suspend fun getTickets(): Response<List<Ticket>>
    
    @GET("tickets/{id}")
    suspend fun getTicket(@Path("id") id: Int): Response<Ticket>
    
    @POST("tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): Response<Map<String, Any>>
    
    @PATCH("tickets/{id}/status")
    suspend fun updateTicketStatus(
        @Path("id") id: Int,
        @Body request: UpdateStatusRequest
    ): Response<Map<String, String>>
    
    @PATCH("tickets/{id}/assign")
    suspend fun assignTicket(
        @Path("id") id: Int,
        @Body request: AssignTicketRequest
    ): Response<Map<String, String>>
    
    @POST("tickets/{id}/rating")
    suspend fun rateTicket(
        @Path("id") id: Int,
        @Body request: RateTicketRequest
    ): Response<Map<String, String>>
    
    @GET("tickets/archive/all")
    suspend fun getArchivedTickets(): Response<List<Ticket>>
    
    @GET("comments/ticket/{ticketId}")
    suspend fun getComments(@Path("ticketId") ticketId: Int): Response<List<Comment>>
    
    @POST("comments")
    suspend fun createComment(@Body request: CreateCommentRequest): Response<Map<String, Any>>
    
    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: Int): Response<Map<String, String>>
    
    @GET("users")
    suspend fun getUsers(@Query("role") role: String? = null): Response<List<User>>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>
    
    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body request: UpdateProfileRequest
    ): Response<Map<String, String>>
    
    @POST("users/support")
    suspend fun createSupport(@Body request: Map<String, String>): Response<Map<String, Any>>
    
    @PATCH("users/{id}/block")
    suspend fun blockUser(
        @Path("id") id: Int,
        @Body request: Map<String, Boolean>
    ): Response<Map<String, String>>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Map<String, String>>
    
    @PUT("users/{id}/admin")
    suspend fun updateUserByAdmin(
        @Path("id") id: Int,
        @Body request: Map<String, Any>
    ): Response<Map<String, String>>
    
    @Multipart
    @POST("tickets/{id}/attachments")
    suspend fun uploadTicketAttachments(
        @Path("id") ticketId: Int,
        @Part images: List<okhttp3.MultipartBody.Part>
    ): Response<AttachmentUploadResponse>
    
    @GET("tickets/{id}/attachments")
    suspend fun getTicketAttachments(@Path("id") ticketId: Int): Response<List<Attachment>>
    
    @Multipart
    @POST("comments/{id}/attachments")
    suspend fun uploadCommentAttachments(
        @Path("id") commentId: Int,
        @Part images: List<okhttp3.MultipartBody.Part>
    ): Response<AttachmentUploadResponse>
    
    @GET("comments/{id}/attachments")
    suspend fun getCommentAttachments(@Path("id") commentId: Int): Response<List<Attachment>>
}
