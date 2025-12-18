package com.example.servicedeskmobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.servicedeskmobile.data.model.UpdateProfileRequest
import com.example.servicedeskmobile.data.model.User
import com.example.servicedeskmobile.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    
    private val repository = UserRepository()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState.asStateFlow()
    
    private var currentRole: String? = null
    
    fun loadUsers(role: String? = null) {
        viewModelScope.launch {
            currentRole = role
            android.util.Log.d("UserViewModel", "loadUsers called with role: $role")
            _userState.value = UserState.Loading
            val result = repository.getUsers(role)
            if (result.isSuccess) {
                val usersList = result.getOrNull() ?: emptyList()
                android.util.Log.d("UserViewModel", "Successfully loaded ${usersList.size} users")
                _users.value = usersList
                _userState.value = UserState.Idle
            } else {
                android.util.Log.e("UserViewModel", "Failed to load users: ${result.exceptionOrNull()?.message}")
                _userState.value = UserState.Error(result.exceptionOrNull()?.message ?: "Ошибка загрузки")
            }
        }
    }
    
    fun updateProfile(id: Int, request: UpdateProfileRequest) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.updateProfile(id, request)
            _userState.value = if (result.isSuccess) {
                UserState.Success(result.getOrNull() ?: "Профиль обновлен")
            } else {
                UserState.Error(result.exceptionOrNull()?.message ?: "Ошибка обновления")
            }
        }
    }
    
    fun createSupport(email: String, password: String, firstName: String, lastName: String, middleName: String?) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.createSupport(email, password, firstName, lastName, middleName)
            _userState.value = if (result.isSuccess) {
                loadUsers("support")
                UserState.Success(result.getOrNull() ?: "Специалист создан")
            } else {
                UserState.Error(result.exceptionOrNull()?.message ?: "Ошибка создания")
            }
        }
    }
    
    fun blockUser(id: Int, isBlocked: Boolean) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.blockUser(id, isBlocked)
            _userState.value = if (result.isSuccess) {
                loadUsers(currentRole)
                UserState.Success(result.getOrNull() ?: "Статус обновлен")
            } else {
                UserState.Error(result.exceptionOrNull()?.message ?: "Ошибка обновления")
            }
        }
    }
    
    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.deleteUser(id)
            _userState.value = if (result.isSuccess) {
                loadUsers(currentRole)
                UserState.Success(result.getOrNull() ?: "Пользователь удален")
            } else {
                UserState.Error(result.exceptionOrNull()?.message ?: "Ошибка удаления")
            }
        }
    }
    
    fun resetState() {
        _userState.value = UserState.Idle
    }
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val message: String) : UserState()
    data class Error(val message: String) : UserState()
}
