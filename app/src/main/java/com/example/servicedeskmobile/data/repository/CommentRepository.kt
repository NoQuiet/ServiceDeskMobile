package com.example.servicedeskmobile.data.repository

import android.content.Context
import android.net.Uri
import com.example.servicedeskmobile.data.api.RetrofitClient
import com.example.servicedeskmobile.data.model.Attachment
import com.example.servicedeskmobile.data.model.Comment
import com.example.servicedeskmobile.data.model.CreateCommentRequest
import com.example.servicedeskmobile.util.ImageUtils

class CommentRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getComments(ticketId: Int): Result<List<Comment>> {
        return try {
            android.util.Log.d("CommentRepository", "Loading comments for ticket $ticketId")
            val response = apiService.getComments(ticketId)
            android.util.Log.d("CommentRepository", "Response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val comments = response.body()!!
                android.util.Log.d("CommentRepository", "Received ${comments.size} comments")
                comments.forEach { comment ->
                    android.util.Log.d("CommentRepository", "Comment: ${comment.message} by ${comment.firstName}")
                }
                Result.success(comments)
            } else {
                android.util.Log.e("CommentRepository", "Error: ${response.errorBody()?.string()}")
                Result.failure(Exception("Ошибка получения комментариев"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createComment(ticketId: Int, message: String, isInternal: Boolean = false): Result<Int?> {
        return try {
            val request = CreateCommentRequest(ticketId, message, isInternal)
            val response = apiService.createComment(request)
            if (response.isSuccessful && response.body() != null) {
                val commentId = (response.body()?.get("id") as? Double)?.toInt()
                Result.success(commentId)
            } else {
                Result.failure(Exception("Ошибка добавления комментария"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadCommentAttachments(commentId: Int, context: Context, imageUris: List<Uri>): Result<String> {
        return try {
            val parts = ImageUtils.urisToMultipartBodyParts(context, imageUris, "images")
            if (parts.isEmpty()) {
                return Result.failure(Exception("Не удалось подготовить изображения"))
            }
            val response = apiService.uploadCommentAttachments(commentId, parts)
            if (response.isSuccessful) {
                Result.success("Фото загружены")
            } else {
                Result.failure(Exception("Ошибка загрузки фото"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCommentAttachments(commentId: Int): Result<List<Attachment>> {
        return try {
            val response = apiService.getCommentAttachments(commentId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения вложений"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(commentId: Int): Result<String> {
        return try {
            val response = apiService.deleteComment(commentId)
            if (response.isSuccessful) {
                Result.success("Комментарий удален")
            } else {
                Result.failure(Exception("Ошибка удаления комментария"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
