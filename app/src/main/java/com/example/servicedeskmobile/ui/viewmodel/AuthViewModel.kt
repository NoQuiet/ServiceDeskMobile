package com.example.servicedeskmobile.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicedeskmobile.data.model.RegisterRequest
import com.example.servicedeskmobile.data.model.User
import com.example.servicedeskmobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {
    
    private val repository = AuthRepository(context)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    val userRole = repository.userRole
    
    init {
        viewModelScope.launch {
            repository.loadAuthToken()
            loadCurrentUser()
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password, "Android")
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()?.user
                AuthState.Success("Вход выполнен")
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Ошибка входа")
            }
        }
    }
    
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(request)
            _authState.value = if (result.isSuccess) {
                AuthState.Success(result.getOrNull() ?: "Регистрация успешна")
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Ошибка регистрации")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            _authState.value = AuthState.LoggedOut
        }
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
        }
    }
    
    fun reloadCurrentUser() {
        loadCurrentUser()
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}
