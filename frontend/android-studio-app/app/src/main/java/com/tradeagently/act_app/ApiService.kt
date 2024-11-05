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
    val session_token: String,
    val review_score: Int,
    val review_comment: String
)

// API response model for login/register response
data class ApiResponse(
    val session_token: String?,
    val status: String?
)

// Data class for username response
data class UsernameResponse(
    val username: String?,
    val status: String?
)

// Data class for email response
data class EmailResponse(
    val email: String?,
    val status: String?
)

// Data class for profile icon response
data class ProfileIconResponse(
    val url: String?,
    val status: String?
)

// API response model for submitting a review
data class ReviewResponse(
    val status: String?
)

data class UserTypeResponse(
    val user_type: String?,
    val status: String?
)

data class SupportTicketRequest(
    val session_token: String,
    val issue_subject: String,
    val issue_description: String
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
    val currency: String,
    val change_percentage: Double,
    val company_description: String,
    val high_price: Double,
    val low_price: Double,
    val open_price: Double,
    val volume: Double,
    val homepage: String
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

// Data class for ticker info request
data class TickerRequest(
    val ticker: String,
    val session_token: String
)

// Data class for ticker aggregates request
data class TickerAggregatesRequest(
    val ticker: String,
    val session_token: String,
    val start_date: String,
    val end_date: String,
    val interval: String,
    val limit: Int
)

// Data class for ticker info response
data class TickerInfoResponse(
    val status: String,
    val ticker_info: TickerInfo?
)


// Ticker info details
data class TickerInfo(
    val change_percentage: Double,
    val close_price: Double,
    val company_description: String?,
    val company_name: String,
    val currency: String,
    val employee_count: Int?,
    val high_price: Double,
    val homepage: String?,
    val low_price: Double,
    val open_price: Double,
    val symbol: String,
    val volume: Double
)

// Data class for ticker aggregates response
data class TickerAggregatesResponse(
    val aggregates: List<TickerAggregate>?,
    val status: String?
)

// Ticker aggregate data item
data class TickerAggregate(
    val v: Double,
    val vw: Double,
    val o: Double,
    val c: Double,
    val h: Double,
    val l: Double,
    val t: Long,
    val n: Int
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

    // Endpoint for getting user type
    @POST("/get-user-type")
    fun getUserType(@Body tokenRequest: TokenRequest): Call<UserTypeResponse>

    // Endpoint for submitting a support ticket
    @POST("/submit-support-ticket")
    fun submitSupportTicket(@Body supportTicketRequest: SupportTicketRequest): Call<ApiResponse>

    // Endpoint for getting client list
    @POST("/get-client-list")
    fun getClientList(@Body tokenRequest: TokenRequest): Call<ClientListResponse>

    // Endpoint for adding a client
    @POST("/add-client")
    fun addClient(@Body addClientRequest: AddClientRequest): Call<ApiResponse>

    // Endpoint for removing a client
    @POST("/remove-client")
    fun removeClient(@Body removeClientRequest: RemoveClientRequest): Call<ApiResponse>

    // Endpoint for getting top stocks
    @GET("/get-top-stocks")
    fun getTopStocks(@Query("limit") limit: Int = 10): Call<TopStocksResponse>

    // Endpoint for searching stocks
    @POST("/text-search-stock")
    fun searchStocks(@Body searchRequest: SearchRequest): Call<StockSearchResponse>

    // Endpoint for deleting a user
    @POST("/delete-user")
    fun deleteUser(@Body requestBody: Map<String, String>): Call<DeleteUserResponse>

    // Endpoint for getting support ticket list
    @POST("/get-support-ticket-list")
    fun getSupportTicketList(@Body tokenRequest: TokenRequest): Call<SupportTicketResponse>

    // Endpoint for getting review list
    @POST("/get-review-list")
    fun getReviewList(@Body tokenRequest: TokenRequest): Call<ReviewListResponse>

    // Endpoint for getting ticker info
    @POST("/get-ticker-info")
    fun getTickerInfo(@Body tickerRequest: TickerRequest): Call<TickerInfoResponse>

    // Endpoint for getting ticker aggregates
    @POST("/get-ticker-aggregates")
    fun getTickerAggregates(@Body request: TickerAggregatesRequest): Call<TickerAggregatesResponse>
}
