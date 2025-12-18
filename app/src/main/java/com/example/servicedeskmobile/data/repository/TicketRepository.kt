package com.example.servicedeskmobile.data.repository

import android.content.Context
import android.net.Uri
import com.example.servicedeskmobile.data.api.RetrofitClient
import com.example.servicedeskmobile.data.model.*
import com.example.servicedeskmobile.util.ImageUtils

class TicketRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getTickets(): Result<List<Ticket>> {
        return try {
            val response = apiService.getTickets()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения заявок"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTicket(id: Int): Result<Ticket> {
        return try {
            val response = apiService.getTicket(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения заявки"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createTicket(title: String, description: String): Result<Int?> {
        return try {
            val request = CreateTicketRequest(title, description)
            val response = apiService.createTicket(request)
            if (response.isSuccessful && response.body() != null) {
                val ticketId = (response.body()?.get("id") as? Double)?.toInt()
                Result.success(ticketId)
            } else {
                Result.failure(Exception("Ошибка создания заявки"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadTicketAttachments(ticketId: Int, context: Context, imageUris: List<Uri>): Result<String> {
        return try {
            val parts = ImageUtils.urisToMultipartBodyParts(context, imageUris, "images")
            if (parts.isEmpty()) {
                return Result.failure(Exception("Не удалось подготовить изображения"))
            }
            val response = apiService.uploadTicketAttachments(ticketId, parts)
            if (response.isSuccessful) {
                Result.success("Фото загружены")
            } else {
                Result.failure(Exception("Ошибка загрузки фото"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTicketStatus(id: Int, status: String): Result<String> {
        return try {
            val request = UpdateStatusRequest(status)
            val response = apiService.updateTicketStatus(id, request)
            if (response.isSuccessful) {
                Result.success("Статус обновлен")
            } else {
                Result.failure(Exception("Ошибка обновления статуса"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun assignTicket(id: Int, supportId: Int? = null): Result<String> {
        return try {
            val request = AssignTicketRequest(supportId)
            val response = apiService.assignTicket(id, request)
            if (response.isSuccessful) {
                Result.success("Заявка назначена")
            } else {
                Result.failure(Exception("Ошибка назначения заявки"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rateTicket(id: Int, rating: Int): Result<String> {
        return try {
            val request = RateTicketRequest(rating)
            val response = apiService.rateTicket(id, request)
            if (response.isSuccessful) {
                Result.success("Оценка сохранена")
            } else {
                Result.failure(Exception("Ошибка сохранения оценки"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getArchivedTickets(): Result<List<Ticket>> {
        return try {
            val response = apiService.getArchivedTickets()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения архива"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTicketAttachments(ticketId: Int): Result<List<Attachment>> {
        return try {
            val response = apiService.getTicketAttachments(ticketId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения вложений"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
