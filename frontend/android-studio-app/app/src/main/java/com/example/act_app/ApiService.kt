package com.example.act_app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class for login request
data class LoginRequest(val email: String, val password: String)

// Data class for register request
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)

// Data class for the token request (used for get-username, get-email, etc.)
data class TokenRequest(val session_token: String)

// API response model for login/register response
data class ApiResponse(
    val session_token: String?,    // Nullable token (could be null on failure)
    val status: String?            // Status message, such as "Success"
)

// Data class for username response
data class UsernameResponse(
    val username: String?,         // Username returned from the API
    val status: String?            // Status message
)

// Data class for email response
data class EmailResponse(
    val email: String?,            // Email returned from the API
    val status: String?            // Status message
)

// Data class for the profile icon response
data class ProfileIconResponse(
    val url: String?,          // Profile icon URL returned from the API
    val status: String?        // Status message
)

// Retrofit interface for API calls
interface ApiService {

    // Endpoint for login
    @POST("/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse>

    // Endpoint for registration
    @POST("/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse>

    // Endpoint for getting the username
    @POST("/get-username")
    fun getUsername(@Body tokenRequest: TokenRequest): Call<UsernameResponse>

    // Endpoint for getting the email
    @POST("/get-email")
    fun getEmail(@Body tokenRequest: TokenRequest): Call<EmailResponse>

    // Endpoint for getting the profile icon
    @POST("/get-profile-icon")
    fun getProfileIcon(@Body tokenRequest: TokenRequest): Call<ProfileIconResponse>

}
