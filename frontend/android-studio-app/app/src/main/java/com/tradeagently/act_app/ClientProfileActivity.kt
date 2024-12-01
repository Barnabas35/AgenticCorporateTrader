package com.tradeagently.act_app

import android.app.AlertDialog
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

class ClientProfileActivity : AppCompatActivity() {

    private lateinit var clientNameTextView: TextView
    private lateinit var clientId: String
    private lateinit var clientName: String
    private lateinit var sessionToken: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonStock: Button
    private lateinit var buttonCrypto: Button
    private var selectedMarket: String = "stocks"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_profile)

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Get session token from shared preferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sessionToken = prefs.getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Get client details from intent
        clientId = intent.getStringExtra("client_id") ?: ""
        clientName = intent.getStringExtra("client_name") ?: ""

        // Initialize views
        clientNameTextView = findViewById(R.id.clientNameTextView)
        recyclerView = findViewById(R.id.recyclerViewClientAssets)
        buttonStock = findViewById(R.id.buttonStock)
        buttonCrypto = findViewById(R.id.buttonCrypto)
        val assetsInfoTextView: TextView = findViewById(R.id.assetsInfoTextView)

        // Set client name
        clientNameTextView.text = clientName
        assetsInfoTextView.text = "Assets owned by $clientName"

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set client name
        clientNameTextView.text = clientName

        // Preselect Stock button
        buttonStock.isSelected = true
        buttonCrypto.isSelected = false

        // Set up button listeners
        buttonStock.setOnClickListener {
            setMarket("stocks")
        }

        buttonCrypto.setOnClickListener {
            setMarket("crypto")
        }

        // Fetch and display client's assets for the default market
        fetchClientAssets()
    }

    private fun setMarket(market: String) {
        selectedMarket = market
        buttonStock.isSelected = market == "stocks"
        buttonCrypto.isSelected = market == "crypto"
        fetchClientAssets()
    }

    private fun fetchClientAssets() {
        val request = GetUserAssetsRequest(sessionToken, clientId, selectedMarket)
        RetrofitClient.apiService.getUserAssets(request).enqueue(object : Callback<UserAssetsResponse> {
            override fun onResponse(call: Call<UserAssetsResponse>, response: Response<UserAssetsResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val assets = response.body()?.ticker_symbols ?: listOf()
                    updateRecyclerViewWithAssets(assets)
                } else {
                    logApiError(response)
                    Toast.makeText(this@ClientProfileActivity, "Failed to fetch $selectedMarket assets.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserAssetsResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching $selectedMarket assets: ${t.message}")
                Toast.makeText(this@ClientProfileActivity, "Error fetching $selectedMarket assets.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecyclerViewWithAssets(assets: List<String>) {
        val noAssetsTextView: TextView = findViewById(R.id.noAssetsTextView) // Find the TextView for no assets message

        if (assets.isEmpty()) {
            // Show the no assets message and hide the RecyclerView
            noAssetsTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            // Set the text based on the selected market
            val selectedMarket = if (buttonStock.isSelected) "stocks" else "crypto"
            noAssetsTextView.text = "This client doesn't own any $selectedMarket."
        } else {
            // Hide the no assets message and show the RecyclerView
            noAssetsTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

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

                val ticker = assets[position]
                assetName.text = ticker

                // Fetch and display asset quantity
                val request = GetAssetRequest(sessionToken, selectedMarket, ticker, clientId)
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

                // Set up Buy and Sell button actions
                buyButton.setOnClickListener { openBuyDialog(ticker) }
                sellButton.setOnClickListener { openSellDialog(ticker) }
            }

            override fun getItemCount() = assets.size
        }

        recyclerView.adapter = adapter
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
            market = selectedMarket,
            ticker = ticker,
            client_id = clientId
        )

        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@ClientProfileActivity, "Successfully bought $ticker!", Toast.LENGTH_SHORT).show()
                    fetchClientAssets() // Refresh asset list
                } else {
                    logApiError(response)
                    Toast.makeText(this@ClientProfileActivity, "Failed to buy $ticker.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error buying $selectedMarket asset: ${t.message}")
                Toast.makeText(this@ClientProfileActivity, "Error buying $selectedMarket asset.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun executeSellTransaction(ticker: String, quantity: Double) {
        val request = SellAssetRequest(
            session_token = sessionToken,
            asset_quantity = quantity,
            market = selectedMarket,
            ticker = ticker,
            client_id = clientId
        )

        RetrofitClient.apiService.sellAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@ClientProfileActivity, "Successfully sold $ticker!", Toast.LENGTH_SHORT).show()
                    fetchClientAssets() // Refresh asset list
                } else {
                    logApiError(response)
                    Toast.makeText(this@ClientProfileActivity, "Failed to sell $ticker.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error selling $selectedMarket asset: ${t.message}")
                Toast.makeText(this@ClientProfileActivity, "Error selling $selectedMarket asset.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logApiError(response: Response<*>) {
        val errorBody = response.errorBody()?.string()
        Log.e("API_ERROR", "Status: ${response.code()}, Error: $errorBody")
    }
}
