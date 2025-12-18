package com.example.servicedeskmobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.servicedeskmobile.data.api.RetrofitClient
import com.example.servicedeskmobile.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.apiService
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        private val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }
    
    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }
    
    suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.get("message") as? String ?: "Регистрация успешна")
            } else {
                Result.failure(Exception("Ошибка регистрации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String, deviceInfo: String? = null): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password, deviceInfo)
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                saveAuthData(loginResponse)
                RetrofitClient.setAuthToken(loginResponse.token)
                Result.success(loginResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val jsonObject = org.json.JSONObject(errorBody)
                        jsonObject.optString("error", "Неверный email или пароль")
                    } else {
                        "Неверный email или пароль"
                    }
                } catch (e: Exception) {
                    "Неверный email или пароль"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<String> {
        return try {
            apiService.logout()
            clearAuthData()
            RetrofitClient.setAuthToken(null)
            Result.success("Выход выполнен")
        } catch (e: Exception) {
            clearAuthData()
            RetrofitClient.setAuthToken(null)
            Result.success("Выход выполнен")
        }
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения данных пользователя"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadAuthToken() {
        val token = authToken.first()
        RetrofitClient.setAuthToken(token)
    }
    
    private suspend fun saveAuthData(loginResponse: LoginResponse) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = loginResponse.token
            preferences[USER_ID_KEY] = loginResponse.user.id.toString()
            preferences[USER_ROLE_KEY] = loginResponse.user.role.name
            preferences[USER_EMAIL_KEY] = loginResponse.user.email
            preferences[USER_FIRST_NAME_KEY] = loginResponse.user.firstName
            preferences[USER_LAST_NAME_KEY] = loginResponse.user.lastName
        }
    }
    
    private suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
