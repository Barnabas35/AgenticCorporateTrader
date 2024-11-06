package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerTextView: TextView = findViewById(R.id.textViewRegister)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter your password"
                return@setOnClickListener
            }

            // Create login request
            val loginRequest = LoginRequest(email, password)

            // Make API call for login
            RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()

                        // Log the raw response for debugging purposes
                        Log.d("API_RESPONSE", "Raw response: $apiResponse")

                        if (apiResponse?.status == "Success" && apiResponse.session_token != null) {
                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                            // Save token to SharedPreferences
                            val token = apiResponse.session_token
                            saveUserDetails(token)

                            // Fetch additional user details and information
                            fetchUserDetails(token)

                            // Navigate to MyAssetsActivity
                            val intent = Intent(this@LoginActivity, MyAssetsActivity::class.java)
                            startActivity(intent)
                            finish() // Close the login activity
                        } else {
                            Log.d("API_RESPONSE_ERROR", "API Message: ${apiResponse?.status}")
                            Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("NETWORK_ERROR", "Failure: ${t.message}")
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Register link click listener
        registerTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Save token and other details to SharedPreferences
    private fun saveUserDetails(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("session_token", token)
        editor.apply()
        Log.d("SessionToken", "Saving token: $token") // Log for debugging
    }

    // Fetch user details after login
    private fun fetchUserDetails(token: String) {
        fetchAndSaveUsername(token)
        fetchAndSaveEmail(token)
        fetchAndSaveProfileIcon(token)
        fetchAndSaveUserType(token)
        fetchAndSaveClientList(token)
        fetchTopStocks(token)
        fetchAndSaveSupportTickets(token)
        fetchAndSaveReviews(token)
    }

    // Fetch and save username
    private fun fetchAndSaveUsername(token: String) {
        RetrofitClient.apiService.getUsername(TokenRequest(token)).enqueue(object : Callback<UsernameResponse> {
            override fun onResponse(call: Call<UsernameResponse>, response: Response<UsernameResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val username = response.body()?.username ?: "Unknown"
                    Log.d("USERNAME_FETCHED", "Fetched username: $username")
                    saveUsername(username)
                } else {
                    Log.e("API_ERROR", "Failed to fetch username: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UsernameResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching username: ${t.message}")
            }
        })
    }

    private fun saveUsername(username: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_name", username)
        editor.apply()
    }

    // Fetch and save email
    private fun fetchAndSaveEmail(token: String) {
        RetrofitClient.apiService.getEmail(TokenRequest(token)).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val email = response.body()?.email ?: "Unknown"
                    Log.d("EMAIL_FETCHED", "Fetched email: $email")
                    saveEmail(email)
                } else {
                    Log.e("API_ERROR", "Failed to fetch email: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching email: ${t.message}")
            }
        })
    }

    private fun saveEmail(email: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email", email)
        editor.apply()
    }

    // Fetch and save profile icon
    private fun fetchAndSaveProfileIcon(token: String) {
        RetrofitClient.apiService.getProfileIcon(TokenRequest(token)).enqueue(object : Callback<ProfileIconResponse> {
            override fun onResponse(call: Call<ProfileIconResponse>, response: Response<ProfileIconResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val profileIconUrl = response.body()?.url ?: "Unknown"
                    Log.d("PROFILE_ICON_FETCHED", "Fetched profile icon URL: $profileIconUrl")
                    saveProfileIconUrl(profileIconUrl)
                } else {
                    Log.e("API_ERROR", "Failed to fetch profile icon URL: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ProfileIconResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching profile icon: ${t.message}")
            }
        })
    }

    private fun saveProfileIconUrl(profileIconUrl: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profile_icon_url", profileIconUrl)
        editor.apply()
    }

    // Fetch and save user type
    private fun fetchAndSaveUserType(token: String) {
        RetrofitClient.apiService.getUserType(TokenRequest(token)).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val userType = response.body()?.user_type ?: "Unknown"
                    Log.d("USER_TYPE_FETCHED", "Fetched user type: $userType")
                    saveUserType(userType)
                } else {
                    Log.e("API_ERROR", "Failed to fetch user type: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching user type: ${t.message}")
            }
        })
    }

    private fun saveUserType(userType: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_type", userType)
        editor.apply()
    }

    // Fetch and save client list
    private fun fetchAndSaveClientList(token: String) {
        RetrofitClient.apiService.getClientList(TokenRequest(token)).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val clients = response.body()?.clients ?: emptyList()
                    Log.d("CLIENT_LIST_FETCHED", "Fetched clients: $clients")
                    saveClientList(clients)
                } else {
                    Log.e("API_ERROR", "Failed to fetch client list: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching client list: ${t.message}")
            }
        })
    }

    private fun saveClientList(clients: List<Client>) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val clientNames = clients.joinToString(",") { it.client_name }
        editor.putString("client_list", clientNames)
        editor.apply()
        Log.d("CLIENT_LIST_SAVED", "Saved clients: $clientNames")
    }

    // Fetch top stocks
    private fun fetchTopStocks(token: String) {
        RetrofitClient.apiService.getTopStocks().enqueue(object : Callback<TopStocksResponse> {
            override fun onResponse(call: Call<TopStocksResponse>, response: Response<TopStocksResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val topStocks = response.body()?.ticker_details ?: emptyList()
                    saveTopStocksToPreferences(topStocks)
                } else {
                    Log.e("TOP_STOCKS_ERROR", "Failed to fetch top stocks: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TopStocksResponse>, t: Throwable) {
                Log.e("TOP_STOCKS_ERROR", "Error fetching top stocks: ${t.message}")
            }
        })
    }

    private fun saveTopStocksToPreferences(stocks: List<StockItem>) {
        val stockData = stocks.joinToString(";") { "${it.symbol},${it.company_name},${it.price},${it.currency}" }
        saveToPreferences("top_stocks", stockData)
    }

    // Fetch and save support tickets
    private fun fetchAndSaveSupportTickets(token: String) {
        RetrofitClient.apiService.getSupportTicketList(TokenRequest(token)).enqueue(object : Callback<SupportTicketResponse> {
            override fun onResponse(call: Call<SupportTicketResponse>, response: Response<SupportTicketResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val tickets = response.body()?.support_tickets ?: emptyList()
                    val ticketData = tickets.joinToString(";") { "${it.issue_subject},${it.issue_description},${it.issue_status}" }
                    saveToPreferences("support_tickets", ticketData)
                } else {
                    Log.e("SUPPORT_TICKETS_ERROR", "Failed to fetch support tickets: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SupportTicketResponse>, t: Throwable) {
                Log.e("SUPPORT_TICKETS_ERROR", "Error fetching support tickets: ${t.message}")
            }
        })
    }

    // Fetch and save reviews
    private fun fetchAndSaveReviews(token: String) {
        RetrofitClient.apiService.getReviewList(TokenRequest(token)).enqueue(object : Callback<ReviewListResponse> {
            override fun onResponse(call: Call<ReviewListResponse>, response: Response<ReviewListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val reviews = response.body()?.reviews ?: emptyList()
                    val reviewData = reviews.joinToString(";") { "${it.score},${it.comment}" }
                    saveToPreferences("reviews", reviewData)
                } else {
                    Log.e("REVIEWS_ERROR", "Failed to fetch reviews: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ReviewListResponse>, t: Throwable) {
                Log.e("REVIEWS_ERROR", "Error fetching reviews: ${t.message}")
            }
        })
    }

    private fun saveToPreferences(key: String, value: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, value).apply()
    }
}
