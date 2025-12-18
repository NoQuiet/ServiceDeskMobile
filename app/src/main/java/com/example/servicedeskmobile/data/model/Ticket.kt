package com.example.servicedeskmobile.data.model

import com.google.gson.annotations.SerializedName

data class Ticket(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("assigned_to")
    val assignedTo: Int? = null,
    val title: String,
    val description: String,
    val status: TicketStatus,
    val priority: TicketPriority,
    val rating: Int? = null,
    val deadline: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("resolved_at")
    val resolvedAt: String? = null,
    @SerializedName("archived_at")
    val archivedAt: String? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val position: String? = null,
    @SerializedName("mobile_phone")
    val mobilePhone: String? = null,
    @SerializedName("internal_phone")
    val internalPhone: String? = null,
    val floor: Int? = null,
    @SerializedName("office_number")
    val officeNumber: String? = null,
    @SerializedName("support_first_name")
    val supportFirstName: String? = null,
    @SerializedName("support_last_name")
    val supportLastName: String? = null
)

enum class TicketStatus {
    new, in_progress, resolved, closed, archived
}

enum class TicketPriority {
    normal, vip
}

data class CreateTicketRequest(
    val title: String,
    val description: String
)

data class UpdateStatusRequest(
    val status: String
)

data class AssignTicketRequest(
    @SerializedName("support_id")
    val supportId: Int? = null
)

data class RateTicketRequest(
    val rating: Int
)
