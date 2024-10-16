package com.example.act_app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class for login request
data class LoginRequest(val email: String, val password: String)

// Data class for register request
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

// API response model for login/register response
data class ApiResponse(
    val success: Boolean,          // 'true' if the operation was successful
    val session_token: String?,    // Nullable token (could be null on failure)
    val message: String?,          // Optional error or success message
    val status: String?            // Optional status, such as "Success"
)

// Retrofit interface for API calls
interface ApiService {

    // Endpoint for login
    @POST("/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse>

    // Endpoint for registration
    @POST("/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse>
}
