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
    val client_id: String
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

data class TickerAggregatesResponse(
    val ticker_info: List<TickerAggregate>?,  // Renamed from 'aggregates' to 'ticker_info'
    val status: String?
)

data class TickerAggregate(
    val close: Double,      // Close price
    val high: Double,       // High price
    val low: Double,        // Low price
    val open: Double,       // Open price
    val volume: Double,     // Volume
    val vwap: Double,       // Volume weighted average price
    val timestamp: Long,    // Unix timestamp
    val transactions: Int   // Number of trades
)

// Request data class for /text-search-crypto
data class TextSearchCryptoRequest(
    val search_query: String,
    val limit: Int = 5,
    val session_token: String,
    val show_price: Boolean
)

// Response data class for /text-search-crypto
data class TextSearchCryptoResponse(
    val crypto_details: List<CryptoItem>,
    val status: String
)

// Request for get crypto info
data class CryptoInfoRequest(
    val crypto: String,
    val session_token: String
)

// Response for get crypto info
data class CryptoInfoResponse(
    val crypto_info: CryptoInfo?,
    val status: String
)

// Response data class for individual crypto info
data class CryptoInfo(
    val description: String,
    val high: Double,
    val latest_price: Double,
    val low: Double,
    val name: String,
    val open: Double,
    val previous_close: Double,
    val symbol: String,
    val volume: Long
)

// Response for get top cryptos and get crypto aggregates
data class TopCryptosResponse(
    val crypto_details: List<CryptoItem>?,
    val status: String
)

// Crypto item data class to hold individual ticker details
data class CryptoItem(
    val symbol: String,
    val name: String,
    val price: Double?
)

// Request for crypto aggregates
data class CryptoAggregatesRequest(
    val crypto: String,
    val session_token: String,
    val start_date: String,
    val end_date: String,
    val interval: String
)

// Response for crypto aggregates
data class CryptoAggregatesResponse(
    val crypto_aggregates: List<CryptoAggregate>,
    val status: String
)

data class CryptoAggregate(
    val close: Double,
    val date: String,
    val high: Double,
    val low: Double,
    val open: Double,
    val volume: Double
)

// Data class for user response in the user list
data class User(
    val client_id: String,
    val username: String,
    val email: String,
    val user_type: String
)

// Response class for user list
data class UserListResponse(
    val status: String,
    val user_list: List<User>
)

// Request class for admin delete user
data class AdminDeleteUserRequest(
    val session_token: String,
    val id: String
)

// Response for balance
data class BalanceResponse(
    val balance: Double,
    val status: String
)

// Request for adding balance
data class AddBalanceRequest(
    val session_token: String,
    val usd_quantity: Int
)

// Request for purchasing an asset
data class PurchaseAssetRequest(
    val session_token: String,
    val usd_quantity: Double,
    val market: String,
    val ticker: String,
    val client_id: String
)

// Request for getting user assets
data class GetUserAssetsRequest(
    val session_token: String,
    val client_id: String,
    val market: String
)

// Response for user assets
data class UserAssetsResponse(
    val status: String,
    val ticker_symbols: List<String>
)

// Request for getting a specific asset
data class GetAssetRequest(
    val session_token: String,
    val market: String,
    val ticker: String,
    val client_id: String
)

// Response for getting an asset
data class AssetResponse(
    val status: String,
    val total_asset_quantity: Double
)

// Request for selling an asset
data class SellAssetRequest(
    val session_token: String,
    val asset_quantity: Double,
    val market: String,
    val ticker: String,
    val client_id: String
)

// Request for asset report
data class AssetReportRequest(
    val session_token: String,
    val market: String,
    val client_id: String,
    val ticker_symbol: String
)

// Response for asset report
data class AssetReportResponse(
    val profit: Double,
    val status: String,
    val total_usd_invested: Double
)

// Data class for creating a price alert
data class SetPriceAlertRequest(
    val session_token: String,
    val ticker: String,
    val price: Double,
    val market: String
)

// Data class for retrieving price alerts
data class GetPriceAlertsRequest(
    val session_token: String
)

// Data class for an individual price alert
data class PriceAlert(
    val alert_id: String,
    val market: String,
    val price: Double,
    val ticker: String
)

// Data class for the response when retrieving price alerts
data class GetPriceAlertsResponse(
    val status: String,
    val alerts: List<PriceAlert>
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

    // Endpoint for sending support ticket solution
    @POST("/resolve-support-ticket")
    fun resolveSupportTicket(@Body request: Map<String, String>): Call<ApiResponse>

    // Endpoint for getting review list
    @POST("/get-review-list")
    fun getReviewList(@Body tokenRequest: TokenRequest): Call<ReviewListResponse>

    // Endpoint for getting ticker info
    @POST("/get-ticker-info")
    fun getTickerInfo(@Body tickerRequest: TickerRequest): Call<TickerInfoResponse>

    // Endpoint for getting ticker aggregates
    @POST("/get-ticker-aggregates")
    fun getTickerAggregates(@Body request: TickerAggregatesRequest): Call<TickerAggregatesResponse>

    // GET request for the top cryptos
    @GET("/get-top-cryptos")
    fun getTopCryptos(
        @Query("limit") limit: Int = 10
    ): Call<TopCryptosResponse>

    // POST request for text search crypto
    @POST("/text-search-crypto")
    fun textSearchCrypto(
        @Body request: TextSearchCryptoRequest
    ): Call<TextSearchCryptoResponse>

    // POST request to get crypto info
    @POST("/get-crypto-info")
    fun getCryptoInfo(
        @Body request: CryptoInfoRequest
    ): Call<CryptoInfoResponse>

    // POST request to get crypto aggregates
    @POST("/get-crypto-aggregates")
    fun getCryptoAggregates(
        @Body request: CryptoAggregatesRequest
    ): Call<CryptoAggregatesResponse>

    // Endpoint for getting the user list as an admin
    @POST("/get-user-list")
    fun getUserList(@Body tokenRequest: TokenRequest): Call<UserListResponse>

    // Endpoint for deleting a user by admin
    @POST("/admin-delete-user")
    fun adminDeleteUser(@Body request: AdminDeleteUserRequest): Call<ApiResponse>

    // Endpoint for getting the balance
    @POST("/get-balance")
    fun getBalance(@Body tokenRequest: TokenRequest): Call<BalanceResponse>

    // Endpoint for adding balance
    @POST("/add-balance")
    fun addBalance(@Body addBalanceRequest: AddBalanceRequest): Call<ApiResponse>

    // Endpoint for purchasing an asset
    @POST("/purchase-asset")
    fun purchaseAsset(@Body purchaseAssetRequest: PurchaseAssetRequest): Call<ApiResponse>

    // Endpoint for getting user assets
    @POST("/get-user-assets")
    fun getUserAssets(@Body getUserAssetsRequest: GetUserAssetsRequest): Call<UserAssetsResponse>

    // Endpoint for getting a specific asset
    @POST("/get-asset")
    fun getAsset(@Body getAssetRequest: GetAssetRequest): Call<AssetResponse>

    // Endpoint for selling an asset
    @POST("/sell-asset")
    fun sellAsset(@Body sellAssetRequest: SellAssetRequest): Call<ApiResponse>

    // Endpoint for getting an asset report
    @POST("/get-asset-report")
    fun getAssetReport(@Body assetReportRequest: AssetReportRequest): Call<AssetReportResponse>

    // Endpoint for creating a price alert
    @POST("/create-price-alert")
    fun createPriceAlert(@Body request: SetPriceAlertRequest): Call<ApiResponse>

    // Endpoint for retrieving price alerts
    @POST("/get-price-alerts")
    fun getPriceAlerts(@Body request: GetPriceAlertsRequest): Call<GetPriceAlertsResponse>

    // Endpoint for deleting a price alert
    @POST("/delete-price-alert")
    fun deletePriceAlert(@Body request: Map<String, String>): Call<ApiResponse>
}
