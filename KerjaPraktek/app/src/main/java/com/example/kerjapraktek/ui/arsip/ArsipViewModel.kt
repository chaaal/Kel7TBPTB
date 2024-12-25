package com.example.kerjapraktek.ui.arsip

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kerjapraktek.data.UserRepository
import com.example.kerjapraktek.data.response.DataArsip
import com.example.kerjapraktek.data.response.DataReply
import com.example.kerjapraktek.ui.requestDetail.DownloadStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArsipViewModel (private val repository: UserRepository,
                      private val context: Context) : ViewModel() {

    private val _archives = MutableStateFlow<List<DataArsip>>(emptyList())
    val archives: StateFlow<List<DataArsip>> = _archives.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()


    init {
        getArchive()
    }

    private fun getArchive() {
        viewModelScope.launch {
            try {
                val response = repository.getArchives()
                // Mengubah response API menjadi daftar KPRequest yang sesuai dengan data yang diinginkan
                _archives.value = response.data

            } catch (e: Exception) {
                // Handle error (optional)
            } finally {
            }
        }
    }



    fun downloadProposal(requestId: String) {
        viewModelScope.launch {
            try {
                _downloadStatus.value = DownloadStatus.Loading

                val response = repository.downloadRequest(requestId)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val contentType = response.headers()["Content-Type"] ?: "application/octet-stream"
                    val fileName = "proposal_$requestId.docx"

                    responseBody?.let {
                        // Create download request using the actual URL
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                        val request = DownloadManager.Request(Uri.parse("https://kerjapraktek.web.id/proposal/$requestId.docx"))
                            .setMimeType(contentType)
                            .setTitle("Downloading Proposal")
                            .setDescription("Downloading proposal file...")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)

                        try {
                            val downloadId = downloadManager.enqueue(request)
                            _downloadStatus.value = DownloadStatus.Success

                            // Optional: Monitor download progress
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

                            // Optional: Monitor download progress
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


}