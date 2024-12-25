package com.example.kerjapraktek.ui.reply

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kerjapraktek.data.UserRepository
import com.example.kerjapraktek.data.response.DataItem
import com.example.kerjapraktek.data.response.DataReply
import com.example.kerjapraktek.ui.requestDetail.DownloadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ReplyViewModel  (private val repository: UserRepository,
                       private val context: Context) : ViewModel() {
    private val _replies = MutableStateFlow<List<DataReply>>(emptyList())
    val replies: StateFlow<List<DataReply>> = _replies.asStateFlow()

    private val _requests = MutableStateFlow<List<DataItem>>(emptyList())
    val requests: StateFlow<List<DataItem>> = _requests.asStateFlow()

    private val _reply = MutableStateFlow<DataReply?>(null)
    val reply: StateFlow<DataReply?> = _reply.asStateFlow()

    private val _addEventResult = MutableLiveData<Boolean>()
    val addEventResult: LiveData<Boolean> get() = _addEventResult

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()


    init {
        getReply()
    }

    fun downloadResponseLetter(requestId: String, companyName: String) {
        viewModelScope.launch {
            try {
                _downloadStatus.value = DownloadStatus.Loading

                val response = repository.downloadReply(requestId)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val contentType = response.headers()["Content-Type"] ?: "application/pdf"  // Ensure it's set to PDF
                    val fileName = "reply_$companyName.pdf"  // Use .pdf extension

                    responseBody?.let {
                        // Create download request using the actual PDF URL
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                        val request = DownloadManager.Request(Uri.parse("https://kerjapraktek.web.id/storage/$requestId"))  // Updated to point to PDF
                            .setMimeType(contentType)
                            .setTitle("Downloading response letter")
                            .setDescription("Downloading response letter file...")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)

                        try {
                            val downloadId = downloadManager.enqueue(request)
                            _downloadStatus.value = DownloadStatus.Success

                            monitorDownload(downloadId)
                        } catch (e: Exception) {
                            _downloadStatus.value = DownloadStatus.Error("Failed to start download: ${e.message}")
                        }
                    }
                } else {
                    _downloadStatus.value = DownloadStatus.Error("Download failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _downloadStatus.value = DownloadStatus.Error("Error downloading proposal: ${e.message}")
            }
        }
    }


    private fun monitorDownload(downloadId: Long) {
        viewModelScope.launch {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                    .query(query)

                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> downloading = false
                        DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            _downloadStatus.value = DownloadStatus.Error("Download failed")
                        }
                    }
                }
                cursor.close()
                delay(1000) // Check every second
            }
        }
    }

    fun getReplyById(id: String) {
        viewModelScope.launch {
            try {
                val response = repository.getReplyById(id)
                // Mengubah response API menjadi KPRequest yang sesuai dengan data yang diinginkan
                _reply.value = response.data

                Log.d("HomeViewModel", "Requests fetched: ${_reply.value}")
            } catch (e: Exception) {
                // Handle error (optional)
            } finally {
            }
        }
    }

    fun fetchReplyList() {
        viewModelScope.launch {
            val data = repository.getReplies().data
            _replies.value = data
            Log.d("HomeViewModel", "Requests fetched: ${_replies.value}")
        }
    }

    private fun getReply() {
        viewModelScope.launch {
            try {
                val response = repository.getReplies()
                // Mengubah response API menjadi daftar KPRequest yang sesuai dengan data yang diinginkan
                _replies.value = response.data

            } catch (e: Exception) {
                // Handle error (optional)
            } finally {
            }
        }
    }

    fun fetchRequestList() {
        viewModelScope.launch {
            val data = repository.getRequests().data
            _requests.value = data.filter { dataItem -> dataItem.status == "Approved" }
            Log.d("HomeViewModellll", "Requests fetched: ${_replies.value}")
        }
    }

    fun addReply(id: String, file : MultipartBody.Part,onSuccess: () -> Unit,
                 onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addReply(id, file)
                _addEventResult.postValue(true)
                onSuccess()
                fetchReplyList() // Refresh data setelah menambah


            } catch (e: Exception) {
                _addEventResult.postValue(false)
                onError(e)
            }
        }
    }



}