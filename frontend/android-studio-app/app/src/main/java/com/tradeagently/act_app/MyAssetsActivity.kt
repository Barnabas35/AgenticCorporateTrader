package com.tradeagently.act_app

import AssetsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAssetsActivity : AppCompatActivity() {

    private lateinit var sessionToken: String
    private lateinit var assetsAdapter: AssetsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var addBalanceButton: Button
    private lateinit var userBalanceTextView: TextView

    private var client_id: String = ""
    private var client_name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_assets)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_my_assets)

        // Get session token
        sessionToken = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerViewAssets)
        userBalanceTextView = findViewById(R.id.userBalance)
        addBalanceButton = findViewById(R.id.addBalanceButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        assetsAdapter = AssetsAdapter(listOf())
        recyclerView.adapter = assetsAdapter

        // Fetch data
        fetchClientList()
        fetchUserBalance()

        // Add balance button
        addBalanceButton.setOnClickListener {
            navigateToAddBalance()
        }
    }

    private fun fetchUserBalance() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getBalance(request).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(call: Call<BalanceResponse>, response: Response<BalanceResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val balance = response.body()?.balance ?: 0.0
                    userBalanceTextView.text = "Balance: $%.2f".format(balance)
                } else {
                    logApiError(response)
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching balance: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error fetching balance.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchClientList() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getClientList(request).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val clients = response.body()?.clients ?: listOf()
                    client_id = clients.firstOrNull()?.client_id ?: ""
                    client_name = clients.firstOrNull()?.client_name ?: ""
                    assetsAdapter.updateAssets(clients.map { it.client_name })
                    if (client_name == "Fund Administrator") {
                        fetchUserAssetsForSelf()
                    }
                } else {
                    logApiError(response)
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching clients: ${t.message}")
            }
        })
    }

    private fun fetchUserAssetsForSelf() {
        val request = GetUserAssetsRequest(sessionToken,client_id ,"stocks")
        RetrofitClient.apiService.getUserAssets(request).enqueue(object : Callback<UserAssetsResponse> {
            override fun onResponse(call: Call<UserAssetsResponse>, response: Response<UserAssetsResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val assets = response.body()?.ticker_symbols ?: listOf()
                    assetsAdapter.updateAssets(assets)
                } else {
                    logApiError(response)
                }
            }

            override fun onFailure(call: Call<UserAssetsResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching assets: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error fetching assets.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToAddBalance() {
        startActivity(Intent(this, AddBalanceActivity::class.java))
        overridePendingTransition(0, 0)
    }

    private fun logApiError(response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        Log.e("API_ERROR", "Status: ${response.code()}, Error: $errorBody")
    }
}

