package com.barraca.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barraca.app.LoginRequest
import com.barraca.app.LoginResponse
import com.barraca.app.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {
    private val apiService = ApiClient.getApiService()
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _user = MutableStateFlow<LoginResponse?>(null)
    val user: StateFlow<LoginResponse?> = _user

    init {
        val savedUser = prefs.getString("user", null)
        if (savedUser != null) {
            try {
                val user = com.google.gson.Gson().fromJson(savedUser, LoginResponse::class.java)
                _user.value = user
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                val response = apiService.login(LoginRequest(email, password))
                
                // Guardar en SharedPreferences
                prefs.edit().putString("user", com.google.gson.Gson().toJson(response)).apply()
                prefs.edit().putInt("user_id", response.id).apply()
                
                _user.value = response
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _user.value = null
        _uiState.value = AuthUiState.Idle
    }

    fun getUserId(): Int {
        return _user.value?.id ?: prefs.getInt("user_id", 0)
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
