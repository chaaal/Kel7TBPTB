package com.example.kerjapraktek.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kerjapraktek.data.UserRepository
import com.example.kerjapraktek.data.pref.UserPreference
import com.example.kerjapraktek.data.response.LoginResponse
import com.example.kerjapraktek.data.response.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel (private val repository: UserRepository) : ViewModel() {
    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _passwordChangeResult = MutableStateFlow<Boolean?>(null)
    val passwordChangeResult: StateFlow<Boolean?> = _passwordChangeResult.asStateFlow()


    fun fetchUser() {
        viewModelScope.launch {
            try {
                val response = repository.getUser()
                _user.value = response
                Log.d("RegisterEventViewModel", "User data fetched: $response")
            } catch (e: Exception) {
                _user.value = null
                // Log error or handle appropriately
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun updatePhone(phone: String) {
        viewModelScope.launch {
            try {
                val response = repository.updatePhone(phone)
                Log.d("ProfileViewModel", "Phone updated: $response")
            } catch (e: Exception) {
                // Log error or handle appropriately
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = repository.changePassword(currentPassword, newPassword)
                if (response.isSuccessful) {
                    _passwordChangeResult.value = true
                    Log.d("ProfileViewModel", "Password updated successfully")
                } else {
                    _passwordChangeResult.value = false
                    Log.e("ProfileViewModel", "Failed to update password: ${response.code()}")
                }
            } catch (e: Exception) {
                _passwordChangeResult.value = false
                Log.e("ProfileViewModel", "Error changing password", e)
            }
        }
    }

    fun resetPasswordChangeResult() {
        _passwordChangeResult.value = null
    }

}