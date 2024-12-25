package com.example.kerjapraktek.data

import android.util.Log
import com.example.kerjapraktek.data.pref.UserModel
import com.example.kerjapraktek.data.pref.UserPreference
import com.example.kerjapraktek.data.remote.ApiService
import com.example.kerjapraktek.data.request.ChangePasswordRequest
import com.example.kerjapraktek.data.request.InfoRequest
import com.example.kerjapraktek.data.request.LoginRequest
import com.example.kerjapraktek.data.request.RegisterRequest
import com.example.kerjapraktek.data.request.RejectRequestBody
import com.example.kerjapraktek.data.response.ArsipResponse
import com.example.kerjapraktek.data.response.InfoResponse
import com.example.kerjapraktek.data.response.InfosResponse


import com.example.kerjapraktek.data.response.LoginResponse
import com.example.kerjapraktek.data.response.RegisterResponse
import com.example.kerjapraktek.data.response.RepliesResponse
import com.example.kerjapraktek.data.response.ReplyResponse
import com.example.kerjapraktek.data.response.RequestResponse
import com.example.kerjapraktek.data.response.RequestsResponse
import com.example.kerjapraktek.data.response.UserResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response


class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(LoginRequest(email, password))
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(RegisterRequest(name, email, password))
    }

    private fun getToken(): String {
        val user = runBlocking { userPreference.getSession().first() }
        Log.d("UserRepository", "Token: ${user.token}")
        return runBlocking { userPreference.getSession().first().token }
    }

    suspend fun getUser(): UserResponse {
        val token = getToken()
        try {
            val dataUser = apiService.getUser("Bearer $token")
            Log.d("UserRepository", "User data: $dataUser")
            return apiService.getUser("Bearer $token")
        } catch (e: HttpException) {
            if (e.code() == 401) {
                logout()
                throw e
            } else {
                throw e
            }
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun updatePhone(phone: String): Response<Void> {
        val token = getToken()
        return apiService.updatePhone(token, phone)
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Response<Void> {
        val token = getToken()
        return apiService.changePassword(
            token = "Bearer $token",
            request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }

    suspend fun getRequests(): RequestsResponse {
        val token = getToken()

        return apiService.getRequests(token)
    }

    suspend fun getRequestById(id: String): RequestResponse {
        val token = getToken()
        return apiService.getRequest(token, id)
    }

    suspend fun rejectRequest(id: String, reason: String): Response<Void> {
        val token = getToken()
        return apiService.rejectRequest(token, id, RejectRequestBody(reason))
    }

    suspend fun acceptRequest( id: String): Response<Void> {
        val token = getToken()
        return apiService.acceptRequest(token, id)
    }

    suspend fun downloadRequest(id: String): Response<ResponseBody> {
        return apiService.downloadRequest( id)
    }

    suspend fun getInfos(): InfosResponse {
        val token = getToken()
        return apiService.getInfos(token)
    }

    suspend fun getInfoById(id: String): InfoResponse {
        val token = getToken()
        return apiService.getInfo(token, id)
    }

    suspend fun addInfo(title: String, description:String ): Response<Void> {
        return apiService.addInfo( InfoRequest(title, description))
    }

    suspend fun getArchives(): ArsipResponse {
        val token = getToken()
        return apiService.getArchives(token)
    }

    suspend fun downloadProposalArchive(id: String): Response<ResponseBody> {
        val token = getToken()
        return apiService.downloadProposalArchive(token, id)
    }

    suspend fun downloadReplyArchive(id: String): Response<ResponseBody> {
        val token = getToken()
        return apiService.downloadReplyArchive(token, id)
    }

    suspend fun getReplies(): RepliesResponse {
        val token = getToken()
        return apiService.getReplies(token)
    }

    suspend fun getReplyById( id: String): ReplyResponse {
        val token = getToken()
        return apiService.getReply(token, id)
    }

    suspend fun downloadReply( id: String): Response<ResponseBody> {
        return apiService.downloadReply( id)
    }

    suspend fun addReply(
        id: String,
        file: MultipartBody.Part
    ): Response<Void> {
        val token = getToken()
        return apiService.addReply(token, id, file)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference)
            }.also { instance = it }
    }
}
