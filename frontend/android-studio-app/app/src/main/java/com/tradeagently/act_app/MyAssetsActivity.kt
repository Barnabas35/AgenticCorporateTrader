package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

    private var userType: String = ""
    private var client_id: String = ""
    private var client_name: String = ""
    private var displayableAssets: List<String> = listOf()

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
        val buttonContainer: View = findViewById(R.id.buttonContainer)
        if (userType == "fm") {
            // Hide the button container for FM users
            buttonContainer.visibility = View.GONE
        } else {
            // Show the button container and set STOCK as the default selected option for FA and Admin
            buttonContainer.visibility = View.VISIBLE
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

                    // Start ClientProfileActivity
                    val intent = Intent(this@MyAssetsActivity, ClientProfileActivity::class.java)
                    intent.putExtra("client_id", selectedClient.client_id)
                    intent.putExtra("client_name", selectedClient.client_name)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }

            override fun getItemCount() = displayableClients.size
        }

        recyclerView.adapter = adapter
    }

    private fun updateRecyclerViewWithAssets(assets: List<String>) {
        displayableAssets = assets // Update the global list
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.asset_item, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val assetName: TextView = holder.itemView.findViewById(R.id.assetName)
                val assetQuantity: TextView = holder.itemView.findViewById(R.id.assetQuantity)
                val buyButton: Button = holder.itemView.findViewById(R.id.buyButton)
                val sellButton: Button = holder.itemView.findViewById(R.id.sellButton)

                val ticker = displayableAssets[position]
                assetName.text = ticker

                // Fetch and display asset quantity
                val request = GetAssetRequest(
                    session_token = sessionToken,
                    market = if (buttonStock.isSelected) "stocks" else "crypto",
                    ticker = ticker,
                    client_id = client_id
                )
                RetrofitClient.apiService.getAsset(request).enqueue(object : Callback<AssetResponse> {
                    override fun onResponse(call: Call<AssetResponse>, response: Response<AssetResponse>) {
                        if (response.isSuccessful && response.body()?.status == "Success") {
                            val quantity = response.body()?.total_asset_quantity ?: 0.0
                            assetQuantity.text = "Owned: %.5f".format(quantity)
                        } else {
                            assetQuantity.text = "Owned: 0.00000"
                            logApiError(response)
                        }
                    }

                    override fun onFailure(call: Call<AssetResponse>, t: Throwable) {
                        assetQuantity.text = "Owned: 0.00000"
                        Log.e("API_ERROR", "Error fetching asset quantity: ${t.message}")
                    }
                })

                // Set up buy and sell buttons
                buyButton.setOnClickListener {
                    openBuyDialog(ticker)
                }

                sellButton.setOnClickListener {
                    openSellDialog(ticker)
                }
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

    private fun openBuyDialog(ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_buy, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Buy $ticker")
            .setView(dialogView)
            .setPositiveButton("Buy") { _, _ ->
                val quantityInput = dialogView.findViewById<EditText>(R.id.quantityInputbuy)
                val quantity = quantityInput.text.toString().toDoubleOrNull()
                if (quantity != null) {
                    executeBuyTransaction(ticker, quantity)
                } else {
                    Toast.makeText(this, "Invalid quantity.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun openSellDialog(ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sell, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Sell $ticker")
            .setView(dialogView)
            .setPositiveButton("Sell") { _, _ ->
                val quantityInput = dialogView.findViewById<EditText>(R.id.quantityInputsell)
                val quantity = quantityInput.text.toString().toDoubleOrNull()
                if (quantity != null) {
                    executeSellTransaction(ticker, quantity)
                } else {
                    Toast.makeText(this, "Invalid quantity.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun executeBuyTransaction(ticker: String, quantity: Double) {
        val request = PurchaseAssetRequest(
            session_token = sessionToken,
            usd_quantity = quantity,
            market = if (buttonStock.isSelected) "stocks" else "crypto",
            ticker = ticker,
            client_id = client_id
        )

        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@MyAssetsActivity, "Successfully bought $ticker!", Toast.LENGTH_SHORT).show()
                    fetchUserAssetsForSelf() // Refresh asset list
                } else {
                    logApiError(response)
                    Toast.makeText(this@MyAssetsActivity, "Failed to buy $ticker.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error buying asset: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error buying asset.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun executeSellTransaction(ticker: String, quantity: Double) {
        val request = SellAssetRequest(
            session_token = sessionToken,
            asset_quantity = quantity,
            market = if (buttonStock.isSelected) "stocks" else "crypto",
            ticker = ticker,
            client_id = client_id
        )

        RetrofitClient.apiService.sellAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@MyAssetsActivity, "Successfully sold $ticker!", Toast.LENGTH_SHORT).show()
                    fetchUserAssetsForSelf() // Refresh asset list
                } else {
                    logApiError(response)
                    Toast.makeText(this@MyAssetsActivity, "Failed to sell $ticker.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error selling asset: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error selling asset.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
