package com.example.kerjapraktek.di

import android.content.Context
import com.example.kerjapraktek.data.UserRepository
import com.example.kerjapraktek.data.pref.UserPreference
import com.example.kerjapraktek.data.pref.dataStore
import com.example.kerjapraktek.data.remote.ApiConfig


    object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()

        return UserRepository.getInstance(apiService, pref)

    }
}