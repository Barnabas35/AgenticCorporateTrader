package com.tradeagently.act_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var addBalanceButton: Button
    private lateinit var userBalanceTextView: TextView
    private lateinit var buttonStock: Button
    private lateinit var buttonCrypto: Button

    private var userType: String = "" // Dynamically fetched user type
    private var client_id: String = ""
    private var client_name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_assets)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_my_assets)

        // Get session token
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sessionToken = prefs.getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerViewAssets)
        userBalanceTextView = findViewById(R.id.userBalance)
        addBalanceButton = findViewById(R.id.addBalanceButton)
        buttonStock = findViewById(R.id.buttonStock)
        buttonCrypto = findViewById(R.id.buttonCrypto)

        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch user type and proceed with fetching data
        fetchUserType()

        // Add balance button
        addBalanceButton.setOnClickListener {
            navigateToAddBalance()
        }

        // Set up button listeners for FA/Admin
        buttonStock.setOnClickListener {
            setButtonSelected(buttonStock, true)
            setButtonSelected(buttonCrypto, false)
            fetchUserAssetsForSelf()
        }

        buttonCrypto.setOnClickListener {
            setButtonSelected(buttonStock, false)
            setButtonSelected(buttonCrypto, true)
            fetchUserAssetsForSelf()
        }
    }

    private fun fetchUserType() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getUserType(request).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    userType = response.body()?.user_type ?: ""
                    setupUI()
                    fetchInitialData()
                } else {
                    logApiError(response)
                    Toast.makeText(this@MyAssetsActivity, "Failed to determine user type.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching user type: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error fetching user type.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupUI() {
        if (userType == "fm") {
            // Hide Stock and Crypto buttons for FM
            buttonStock.visibility = View.GONE
            buttonCrypto.visibility = View.GONE
        } else {
            // Set STOCK as the default selected option for FA and Admin
            setButtonSelected(buttonStock, true)
            setButtonSelected(buttonCrypto, false)
        }
    }

    private fun fetchInitialData() {
        // Fetch user balance
        fetchUserBalance()

        // Fetch clients or assets based on user type
        when (userType) {
            "fm" -> fetchClientList() // FM sees client list
            "fa" -> fetchClientListAndSelectFirstClient() // FA automatically fetches assets for the first client
            else -> fetchUserAssetsForSelf() // Admin directly fetches user assets
        }
    }

    private fun setButtonSelected(button: Button, isSelected: Boolean) {
        button.isSelected = isSelected
        button.setTextColor(resources.getColor(if (isSelected) android.R.color.white else android.R.color.white))
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
                    updateRecyclerViewWithClients(clients)
                } else {
                    logApiError(response)
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching clients: ${t.message}")
            }
        })
    }

    private fun fetchClientListAndSelectFirstClient() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getClientList(request).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val clients = response.body()?.clients ?: listOf()
                    val defaultClient = clients.firstOrNull()
                    if (defaultClient != null) {
                        client_id = defaultClient.client_id
                        client_name = defaultClient.client_name
                        fetchUserAssetsForSelf()
                    } else {
                        Toast.makeText(this@MyAssetsActivity, "No clients available.", Toast.LENGTH_SHORT).show()
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
        if (client_id.isEmpty() && userType == "fa") {
            Toast.makeText(this, "No client selected.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMarket = if (buttonStock.isSelected) "stocks" else "crypto"
        val request = GetUserAssetsRequest(sessionToken, client_id, selectedMarket)

        RetrofitClient.apiService.getUserAssets(request).enqueue(object : Callback<UserAssetsResponse> {
            override fun onResponse(call: Call<UserAssetsResponse>, response: Response<UserAssetsResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val assets = response.body()?.ticker_symbols ?: listOf()
                    updateRecyclerViewWithAssets(assets)
                } else {
                    logApiError(response)
                }
            }

            override fun onFailure(call: Call<UserAssetsResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching assets: ${t.message}")
            }
        })
    }

    private fun updateRecyclerViewWithClients(clients: List<Client>) {
        val displayableClients = clients.map { it.client_name }

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.client_item, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val clientName: TextView = holder.itemView.findViewById(R.id.clientName)
                clientName.text = displayableClients[position]

                holder.itemView.setOnClickListener {
                    val selectedClient = clients[position]
                    client_id = selectedClient.client_id
                    client_name = selectedClient.client_name
                    fetchUserAssetsForSelf()
                }
            }

            override fun getItemCount() = displayableClients.size
        }

        recyclerView.adapter = adapter
    }

    private fun updateRecyclerViewWithAssets(assets: List<String>) {
        val displayableAssets = assets.map { it }

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.asset_item, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val assetName: TextView = holder.itemView.findViewById(R.id.assetName)
                assetName.text = displayableAssets[position]
            }

            override fun getItemCount() = displayableAssets.size
        }

        recyclerView.adapter = adapter
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
