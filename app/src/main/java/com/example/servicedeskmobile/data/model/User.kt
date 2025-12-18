package com.example.servicedeskmobile.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val email: String,
    val role: UserRole,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String? = null,
    @SerializedName("mobile_phone")
    val mobilePhone: String? = null,
    @SerializedName("internal_phone")
    val internalPhone: String? = null,
    val floor: Int? = null,
    @SerializedName("office_number")
    val officeNumber: String? = null,
    val position: String,
    @SerializedName("is_blocked")
    val isBlocked: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null
)

enum class UserRole {
    admin, support, user
}

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_info")
    val deviceInfo: String? = null
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String? = null,
    @SerializedName("mobile_phone")
    val mobilePhone: String? = null,
    @SerializedName("internal_phone")
    val internalPhone: String? = null,
    val floor: Int? = null,
    @SerializedName("office_number")
    val officeNumber: String? = null,
    val position: String
)

data class UpdateProfileRequest(
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("middle_name")
    val middleName: String? = null,
    @SerializedName("mobile_phone")
    val mobilePhone: String? = null,
    @SerializedName("internal_phone")
    val internalPhone: String? = null,
    val floor: Int? = null,
    @SerializedName("office_number")
    val officeNumber: String? = null,
    val position: String? = null
)
