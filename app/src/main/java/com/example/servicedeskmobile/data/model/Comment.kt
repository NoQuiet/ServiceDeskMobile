package com.example.servicedeskmobile.data.model

import com.google.gson.annotations.SerializedName

data class Comment(
    val id: Int,
    @SerializedName("ticket_id")
    val ticketId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val message: String,
    @SerializedName("is_internal")
    val isInternal: Int = 0,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val role: String
)

data class CreateCommentRequest(
    @SerializedName("ticket_id")
    val ticketId: Int,
    val message: String,
    @SerializedName("is_internal")
    val isInternal: Boolean = false
)
