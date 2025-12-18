package com.example.servicedeskmobile.data.repository

import com.example.servicedeskmobile.data.api.RetrofitClient
import com.example.servicedeskmobile.data.model.UpdateProfileRequest
import com.example.servicedeskmobile.data.model.User

class UserRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getUsers(role: String? = null): Result<List<User>> {
        return try {
            val response = apiService.getUsers(role)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения пользователей"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(id: Int): Result<User> {
        return try {
            val response = apiService.getUser(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения пользователя"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(id: Int, request: UpdateProfileRequest): Result<String> {
        return try {
            val response = apiService.updateProfile(id, request)
            if (response.isSuccessful) {
                Result.success("Профиль обновлен")
            } else {
                Result.failure(Exception("Ошибка обновления профиля"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createSupport(email: String, password: String, firstName: String, lastName: String, middleName: String?): Result<String> {
        return try {
            val request = mutableMapOf(
                "email" to email,
                "password" to password,
                "first_name" to firstName,
                "last_name" to lastName
            )
            middleName?.let { request["middle_name"] = it }
            
            val response = apiService.createSupport(request)
            if (response.isSuccessful) {
                Result.success("Специалист создан")
            } else {
                Result.failure(Exception("Ошибка создания специалиста"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun blockUser(id: Int, isBlocked: Boolean): Result<String> {
        return try {
            val response = apiService.blockUser(id, mapOf("is_blocked" to isBlocked))
            if (response.isSuccessful) {
                Result.success(if (isBlocked) "Пользователь заблокирован" else "Пользователь разблокирован")
            } else {
                Result.failure(Exception("Ошибка блокировки пользователя"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(id: Int): Result<String> {
        return try {
            val response = apiService.deleteUser(id)
            if (response.isSuccessful) {
                Result.success("Пользователь удален")
            } else {
                Result.failure(Exception("Ошибка удаления пользователя"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
