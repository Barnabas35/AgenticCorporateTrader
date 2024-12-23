package com.tradeagently.act_app

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val user_type: String
)

data class TokenRequest(val session_token: String)

data class ReviewRequest(
    val session_token: String,
    val review_score: Int,
    val review_comment: String
)

data class ApiResponse(
    val session_token: String?,
    val status: String?
)

data class UsernameResponse(
    val username: String?,
    val status: String?
)

data class EmailResponse(
    val email: String?,
    val status: String?
)

data class ProfileIconResponse(
    val url: String?,
    val status: String?
)

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

data class TickerRequest(
    val ticker: String,
    val session_token: String
)

data class TickerAggregatesRequest(
    val ticker: String,
    val session_token: String,
    val start_date: String,
    val end_date: String,
    val interval: String,
    val limit: Int
)

data class TickerInfoResponse(
    val status: String,
    val ticker_info: TickerInfo?
)

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
    val ticker_info: List<TickerAggregate>?,
    val status: String?
)

data class TickerAggregate(
    val close: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val volume: Double,
    val vwap: Double,
    val timestamp: Long,
    val transactions: Int
)

data class TextSearchCryptoRequest(
    val search_query: String,
    val limit: Int = 5,
    val session_token: String,
    val show_price: Boolean
)

data class TextSearchCryptoResponse(
    val crypto_details: List<CryptoItem>,
    val status: String
)

data class CryptoInfoRequest(
    val crypto: String,
    val session_token: String
)

data class CryptoInfoResponse(
    val crypto_info: CryptoInfo?,
    val status: String
)

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

data class TopCryptosResponse(
    val crypto_details: List<CryptoItem>?,
    val status: String
)

data class CryptoItem(
    val symbol: String,
    val name: String,
    val price: Double?
)

data class CryptoAggregatesRequest(
    val crypto: String,
    val session_token: String,
    val start_date: String,
    val end_date: String,
    val interval: String
)

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

data class User(
    val client_id: String,
    val username: String,
    val email: String,
    val user_type: String
)

data class UserListResponse(
    val status: String,
    val user_list: List<User>
)

data class AdminDeleteUserRequest(
    val session_token: String,
    val id: String
)

data class AddBalanceResponse(
    val status: String,
    val client_secret: String? = null
)

data class AddBalanceRequest(
    val session_token: String,
    val usd_quantity: Int
)

data class BalanceResponse(
    val balance: Double,
    val status: String
)

data class PurchaseAssetRequest(
    val session_token: String,
    val usd_quantity: Double,
    val market: String,
    val ticker: String,
    val client_id: String
)

data class GetUserAssetsRequest(
    val session_token: String,
    val client_id: String,
    val market: String
)

data class UserAssetsResponse(
    val status: String,
    val ticker_symbols: List<String>
)

data class GetAssetRequest(
    val session_token: String,
    val market: String,
    val ticker: String,
    val client_id: String
)

data class AssetResponse(
    val status: String,
    val total_asset_quantity: Double
)

data class SellAssetRequest(
    val session_token: String,
    val asset_quantity: Double,
    val market: String,
    val ticker: String,
    val client_id: String
)

data class SetPriceAlertRequest(
    val session_token: String,
    val ticker: String,
    val price: Double,
    val market: String
)

data class GetPriceAlertsRequest(
    val session_token: String
)

data class PriceAlert(
    val alert_id: String,
    val market: String,
    val price: Double,
    val ticker: String
)

data class GetPriceAlertsResponse(
    val status: String,
    val alerts: List<PriceAlert>
)

data class AssetReportRequest(
    val session_token: String,
    val market: String,
    val client_id: String,
    val ticker: String
)

data class AssetReportResponse(
    val profit: Double,
    val status: String,
    val total_usd_invested: Double
)

data class ExchangeTokensRequest(
    val auth_token: String
)

data class ExchangeTokensResponse(
    val session_token: String?,
    val status: String
)

data class RegisterWithTokenRequest(
    val auth_token: String,
    val user_type: String
)

data class RegisterWithTokenResponse(
    val status: String
)

data class AiAssetReportRequest(
    val session_token: String,
    val market: String,
    val ticker: String
)

data class AiAssetReportResponse(
    val response: String,
    val status: String,
    val future: String?,
    val recommend: String?
)

data class SubscriptionRequest(
    val session_token: String
)

data class SubscriptionResponse(
    val status: String,
    val subscription_start: Long,
    val subscription_end: Long,
    val subscription_active: Boolean,
    val renew_subscription: Boolean
)

data class SubscriptionActionResponse(
    val status: String
)

data class AiAccountingRequest(
    val session_token: String,
    val market: String,
    val ticker: String,
    val client_id: String
)

data class AiAccountingResponse(
    val asset_growth: String,
    val asset_liquidity: String,
    val asset_profitability: String,
)

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
    fun getTopCryptos(@Query("limit") limit: Int = 10): Call<TopCryptosResponse>

    // POST request for text search crypto
    @POST("/text-search-crypto")
    fun textSearchCrypto(@Body request: TextSearchCryptoRequest): Call<TextSearchCryptoResponse>

    // POST request to get crypto info
    @POST("/get-crypto-info")
    fun getCryptoInfo(@Body request: CryptoInfoRequest): Call<CryptoInfoResponse>

    // POST request to get crypto aggregates
    @POST("/get-crypto-aggregates")
    fun getCryptoAggregates(@Body request: CryptoAggregatesRequest): Call<CryptoAggregatesResponse>

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
    fun addBalance(@Body addBalanceRequest: AddBalanceRequest): Call<AddBalanceResponse>

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

    // Endpoint for creating a price alert
    @POST("/create-price-alert")
    fun createPriceAlert(@Body request: SetPriceAlertRequest): Call<ApiResponse>

    // Endpoint for retrieving price alerts
    @POST("/get-price-alerts")
    fun getPriceAlerts(@Body request: GetPriceAlertsRequest): Call<GetPriceAlertsResponse>

    // Endpoint for deleting a price alert
    @POST("/delete-price-alert")
    fun deletePriceAlert(@Body request: Map<String, String>): Call<ApiResponse>

    // Endpoint for getting an asset report
    @POST("/get-asset-report")
    fun getAssetReport(@Body assetReportRequest: AssetReportRequest): Call<AssetReportResponse>

    // Endpoint for exchanging tokens
    @POST("/exchange-tokens")
    fun exchangeTokens(@Body request: ExchangeTokensRequest): Call<ExchangeTokensResponse>

    // Endpoint for registering with a token
    @POST("/register-with-token")
    fun registerWithToken(@Body request: RegisterWithTokenRequest): Call<RegisterWithTokenResponse>

    // Endpoint for getting AI asset report
    @POST("/get-ai-asset-report")
    fun getAiAssetReport(@Body request: AiAssetReportRequest): Call<AiAssetReportResponse>

    // Endpoint for getting subscription details
    @POST("/get-subscription")
    fun getSubscription(@Body request: SubscriptionRequest): Call<SubscriptionResponse>

    // Endpoint for canceling a subscription
    @POST("/cancel-subscription")
    fun cancelSubscription(@Body request: SubscriptionRequest): Call<SubscriptionActionResponse>

    // Endpoint for activating a subscription
    @POST("/activate-subscription")
    fun activateSubscription(@Body request: SubscriptionRequest): Call<SubscriptionActionResponse>

    // Endpoint for getting AI accounting data
    @POST("/get-ai-accounting")
    fun getAiAccounting(@Body request: AiAccountingRequest): Call<AiAccountingResponse>
}
