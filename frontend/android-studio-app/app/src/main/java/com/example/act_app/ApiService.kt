package com.example.act_app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String, val confirmPassword: String)
data class ApiResponse(val success: Boolean, val session_token: String?, val message: String?, val status: String?)

interface ApiService {

    @POST("/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse>

    @POST("/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse>
}
