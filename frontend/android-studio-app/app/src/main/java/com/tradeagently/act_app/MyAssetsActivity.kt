package com.tradeagently.act_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tradeagently.act_app.RetrofitClient.apiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAssetsActivity : AppCompatActivity() {

    private lateinit var assetsAdapter: AssetsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var addBalanceButton: Button
    private lateinit var userBalanceTextView: TextView

    private var userType: String = ""
    private var clientId: String = "" // Dynamically set based on user type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_assets)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_my_assets)

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerViewAssets)
        userBalanceTextView = findViewById(R.id.userBalance)
        addBalanceButton = findViewById(R.id.addBalanceButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        assetsAdapter = AssetsAdapter(listOf())
        recyclerView.adapter = assetsAdapter

        // Set up Add Balance Button
        addBalanceButton.setOnClickListener {
            navigateToAddBalance()
        }

        // Fetch user balance and user type
        fetchUserBalance()
        fetchUserType()
    }

    private fun getSessionToken(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("session_token", "") ?: ""
    }

    private fun fetchUserBalance() {
        val sessionToken = getSessionToken()

        apiService.getBalance(TokenRequest(sessionToken)).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(call: Call<BalanceResponse>, response: Response<BalanceResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val balance = response.body()?.balance ?: 0.0
                    userBalanceTextView.text = "Balance: $%.2f".format(balance)
                } else {
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch balance", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                Toast.makeText(this@MyAssetsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserType() {
        val sessionToken = getSessionToken()

        apiService.getUserType(TokenRequest(sessionToken)).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    userType = response.body()?.user_type ?: ""
                    handleUserType(userType)
                } else {
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch user type", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Toast.makeText(this@MyAssetsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleUserType(userType: String) {
        when (userType) {
            "fm" -> fetchClientList()
            "fa", "admin" -> fetchUserAssetsForSelf()
            else -> Toast.makeText(this, "Unsupported user type: $userType", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchClientList() {
        val sessionToken = getSessionToken()

        apiService.getClientList(TokenRequest(sessionToken)).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val clients = response.body()?.clients ?: listOf()
                    clientId = clients.firstOrNull()?.id ?: "" // Use the first client as default
                    val clientNames = clients.map { it.client_name }
                    assetsAdapter.updateAssets(clientNames)
                } else {
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch client list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Toast.makeText(this@MyAssetsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserAssetsForSelf() {
        val sessionToken = getSessionToken()

        apiService.getUserAssets(GetUserAssetsRequest(sessionToken, sessionToken, "stocks"))
            .enqueue(object : Callback<UserAssetsResponse> {
                override fun onResponse(
                    call: Call<UserAssetsResponse>,
                    response: Response<UserAssetsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val assets = response.body()?.ticker_symbols ?: listOf()
                        assetsAdapter.updateAssets(assets)
                    } else {
                        Toast.makeText(
                            this@MyAssetsActivity,
                            "Failed to fetch assets: ${response.body()?.status}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserAssetsResponse>, t: Throwable) {
                    Toast.makeText(this@MyAssetsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun navigateToAddBalance() {
        val intent = Intent(this, AddBalanceActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
