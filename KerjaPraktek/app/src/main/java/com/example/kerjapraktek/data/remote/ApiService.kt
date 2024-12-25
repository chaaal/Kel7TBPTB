package com.example.kerjapraktek.data.remote


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
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiService {

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse


    @GET("/user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): UserResponse

    @POST("/update-phone")
    suspend fun updatePhone(
        @Header("Authorization") token: String,
        @Body phone: String
    ): Response<Void>

    @POST("/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Void>
    @GET("/requests")
    suspend fun getRequests(
        @Header("Authorization") token: String
    ): RequestsResponse

    @GET("/requests/{id}")
    suspend fun getRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): RequestResponse

    @POST("/requests/{id}/reject")
    suspend fun rejectRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: RejectRequestBody
    ): Response<Void>

    @POST("/requests/{id}/accept")
    suspend fun acceptRequest(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Void>

    @GET("/proposal/{id}.docx")
    @Streaming
    suspend fun downloadRequest(@Path("id") requestId: String): Response<ResponseBody>

    // Infos
    @GET("/infos")
    suspend fun getInfos(
        @Header("Authorization") token: String
    ): InfosResponse

    @GET("/infos/{id}")
    suspend fun getInfo(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): InfoResponse

    @POST("/infos")
    suspend fun addInfo(
        @Body infoRequest: InfoRequest
    ): Response<Void>

    // Archives
    @GET("/archives")
    suspend fun getArchives(
        @Header("Authorization") token: String
    ): ArsipResponse

    @GET("/archives/{id}/downloadProposal")
    suspend fun downloadProposalArchive(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>

    @GET("/archives/{id}/downloadReply")
    suspend fun downloadReplyArchive(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ResponseBody>


    @GET("/storage/{id}")
    @Streaming
    suspend fun downloadReply(@Path("id") requestId: String): Response<ResponseBody>

    // Replies
    @GET("/replies")
    suspend fun getReplies(
        @Header("Authorization") token: String
    ): RepliesResponse

    @GET("/replies/{id}")
    suspend fun getReply(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ReplyResponse



    @Multipart
    @POST("/replies/{id}")
    suspend fun addReply(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<Void>
}



