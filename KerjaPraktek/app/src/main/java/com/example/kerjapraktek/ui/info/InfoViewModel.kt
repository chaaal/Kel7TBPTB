package com.example.kerjapraktek.ui.info

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kerjapraktek.data.UserRepository
import com.example.kerjapraktek.data.response.DataInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InfoViewModel (private val repository: UserRepository) : ViewModel() {

    // StateFlow untuk menangani daftar request

    private val _infos = MutableStateFlow<List<DataInfo>>(emptyList())
    val infos: StateFlow<List<DataInfo>> = _infos.asStateFlow()

    private val _info = MutableStateFlow<DataInfo?>(null)
    val info: StateFlow<DataInfo?> = _info.asStateFlow()

    init {
        getInfo()
    }

    private fun getInfo() {
        viewModelScope.launch {
            try {
                val response = repository.getInfos()
                // Mengubah response API menjadi daftar KPRequest yang sesuai dengan data yang diinginkan
                _infos.value = response.data

                Log.d("HomeViewModel", "Requests fetched: ${_infos.value}")
            } catch (e: Exception) {
                // Handle error (optional)
            } finally {
            }
        }
    }

    fun getInfoById(id: String) {
        viewModelScope.launch {
            try {
                val response = repository.getInfoById(id)
                // Mengubah response API menjadi KPRequest yang sesuai dengan data yang diinginkan
                _info.value = response.data

                Log.d("HomeViewModel", "Requests fetched: ${_info.value}")
            } catch (e: Exception) {
                // Handle error (optional)
            } finally {
            }
        }
    }

    fun fetchInfoList() {
        viewModelScope.launch {
            val data = repository.getInfos().data
            _infos.value = data
            Log.d("HomeViewModel", "Requests fetched: ${_infos.value}")
        }
    }

    fun addInfo(title: String, description: String) {
        viewModelScope.launch {
            try {
                val response = repository.addInfo(title, description)

                fetchInfoList() // Refresh data setelah menambah


            } catch (e: Exception) {
                // Handle error (optional)
            } finally {
            }
        }
    }



}