package com.tradeagently.act_app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data class for login request
data class LoginRequest(val email: String, val password: String)

// Data class for register request
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val user_type: String
)

// Data class for the token request (used for get-username, get-email, etc.)
data class TokenRequest(val session_token: String)

// Data class for submitting a review
data class ReviewRequest(
    val session_token: String,     // Session token to authenticate the user
    val review_score: Int,         // Review score, an integer from 1 to 5
    val review_comment: String     // Review comment text
)

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
    val url: String?,              // Profile icon URL returned from the API
    val status: String?            // Status message
)

// API response model for submitting a review
data class ReviewResponse(
    val status: String?            // Status message, such as "Success" or "Error"
)

data class UserTypeResponse(
    val user_type: String?,       // The user type, such as "admin", "fa", or "fm"
    val status: String?           // Status message, such as "Success"
)

data class SupportTicketRequest(
    val session_token: String,        // The user's session token
    val issue_subject: String,        // Subject of the issue
    val issue_description: String     // Detailed description of the issue
)

data class AddClientRequest(val session_token: String, val client_name: String)

data class RemoveClientRequest(val session_token: String, val client_name: String)

data class ClientListResponse(
    val clients: List<Client>,
    val status: String
)

data class Client(
    val client_name: String,
    val id: String
)

// Unified StockItem class for both top stocks and search results
data class TopStocksResponse(
    val ticker_details: List<StockItem>,
    val status: String
)

data class StockItem(
    val symbol: String,
    val company_name: String,
    val price: Double,
    val currency: String
)

data class SearchRequest(
    val search_query: String,
    val limit: Int = 5,
    val session_token: String,
    val show_price: Boolean = true
)

data class StockSearchResponse(
    val ticker_details: List<StockItem>,
    val status: String
)

data class DeleteUserResponse(
    val status: String
)

data class SupportTicketResponse(
    val support_tickets: List<SupportTicket>,
    val status: String
)

data class SupportTicket(
    val issue_subject: String,
    val user_id: String,
    val issue_description: String,
    val issue_status: String,
    val ticket_id: String,
    val unix_timestamp: Long
)

data class ReviewListResponse(
    val reviews: List<Review>,
    val status: String
)

data class Review(
    val score: Int,
    val comment: String,
    val user_id: String
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

    // Endpoint for submitting a review
    @POST("/submit-review")
    fun submitReview(@Body reviewRequest: ReviewRequest): Call<ReviewResponse>

    @POST("/get-user-type")
    fun getUserType(@Body tokenRequest: TokenRequest): Call<UserTypeResponse>

    @POST("/submit-support-ticket")
    fun submitSupportTicket(@Body supportTicketRequest: SupportTicketRequest): Call<ApiResponse>

    @POST("/get-client-list")
    fun getClientList(@Body tokenRequest: TokenRequest): Call<ClientListResponse>

    @POST("/add-client")
    fun addClient(@Body addClientRequest: AddClientRequest): Call<ApiResponse>

    @POST("/remove-client")
    fun removeClient(@Body removeClientRequest: RemoveClientRequest): Call<ApiResponse>

    @GET("/get-top-stocks")
    fun getTopStocks(@Query("limit") limit: Int = 10): Call<TopStocksResponse>

    @POST("/text-search-stock")
    fun searchStocks(@Body searchRequest: SearchRequest): Call<StockSearchResponse>

    @POST("/delete-user")
    fun deleteUser(@Body requestBody: Map<String, String>): Call<DeleteUserResponse>

    @POST("/get-support-ticket-list")
    fun getSupportTicketList(@Body tokenRequest: TokenRequest): Call<SupportTicketResponse>

    @POST("/get-review-list")
    fun getReviewList(@Body tokenRequest: TokenRequest): Call<ReviewListResponse>


}
