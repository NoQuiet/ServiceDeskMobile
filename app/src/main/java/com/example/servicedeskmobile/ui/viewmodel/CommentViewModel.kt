package com.example.servicedeskmobile.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicedeskmobile.data.model.Attachment
import com.example.servicedeskmobile.data.model.Comment
import com.example.servicedeskmobile.data.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    
    private val repository = CommentRepository()
    
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()
    
    private val _commentState = MutableStateFlow<CommentState>(CommentState.Idle)
    val commentState: StateFlow<CommentState> = _commentState.asStateFlow()
    
    private val _commentAttachments = MutableStateFlow<Map<Int, List<Attachment>>>(emptyMap())
    val commentAttachments: StateFlow<Map<Int, List<Attachment>>> = _commentAttachments.asStateFlow()
    
    fun loadComments(ticketId: Int) {
        viewModelScope.launch {
            android.util.Log.d("CommentViewModel", "loadComments called for ticket $ticketId")
            _commentState.value = CommentState.Loading
            val result = repository.getComments(ticketId)
            if (result.isSuccess) {
                val comments = result.getOrNull() ?: emptyList()
                android.util.Log.d("CommentViewModel", "Successfully loaded ${comments.size} comments")
                _comments.value = comments
                _commentState.value = CommentState.Idle
            } else {
                android.util.Log.e("CommentViewModel", "Failed to load comments: ${result.exceptionOrNull()?.message}")
                _commentState.value = CommentState.Error(result.exceptionOrNull()?.message ?: "Ошибка загрузки")
            }
        }
    }
    
    fun addComment(ticketId: Int, message: String, isInternal: Boolean = false, context: Context? = null, imageUris: List<Uri>? = null) {
        viewModelScope.launch {
            _commentState.value = CommentState.Loading
            val result = repository.createComment(ticketId, message, isInternal)
            if (result.isSuccess) {
                val commentId = result.getOrNull()
                if (commentId != null && context != null && !imageUris.isNullOrEmpty()) {
                    repository.uploadCommentAttachments(commentId, context, imageUris)
                }
                kotlinx.coroutines.delay(300)
                loadComments(ticketId)
                _commentState.value = CommentState.Success("Комментарий добавлен")
            } else {
                _commentState.value = CommentState.Error(result.exceptionOrNull()?.message ?: "Ошибка добавления")
            }
        }
    }
    
    fun loadCommentAttachments(commentId: Int) {
        viewModelScope.launch {
            val result = repository.getCommentAttachments(commentId)
            if (result.isSuccess) {
                val attachments = result.getOrNull() ?: emptyList()
                _commentAttachments.value = _commentAttachments.value + (commentId to attachments)
            }
        }
    }
    
    fun deleteComment(commentId: Int, ticketId: Int) {
        viewModelScope.launch {
            android.util.Log.d("CommentViewModel", "Deleting comment $commentId")
            _commentState.value = CommentState.Loading
            val result = repository.deleteComment(commentId)
            if (result.isSuccess) {
                android.util.Log.d("CommentViewModel", "Comment deleted successfully")
                loadComments(ticketId)
                _commentState.value = CommentState.Success(result.getOrNull() ?: "Комментарий удален")
            } else {
                android.util.Log.e("CommentViewModel", "Failed to delete comment: ${result.exceptionOrNull()?.message}")
                _commentState.value = CommentState.Error(result.exceptionOrNull()?.message ?: "Ошибка удаления")
            }
        }
    }
    
    fun resetState() {
        _commentState.value = CommentState.Idle
    }
}

sealed class CommentState {
    object Idle : CommentState()
    object Loading : CommentState()
    data class Success(val message: String, val data: Any? = null) : CommentState()
    data class Error(val message: String) : CommentState()
}
