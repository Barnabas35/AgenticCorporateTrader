package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
                val settingsButton: ImageButton = holder.itemView.findViewById(R.id.settingsButton)

                val ticker = assets[position]
                assetName.text = ticker

                // Fetch and display asset quantity
                val request = GetAssetRequest(session_token = sessionToken, market = selectedMarket, ticker = ticker, client_id = clientId)
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

                buyButton.setOnClickListener {
                    openBuyDialog(ticker)
                }

                sellButton.setOnClickListener {
                    openSellDialog(ticker)
                }

                settingsButton.setOnClickListener {
                    val options = arrayOf("Asset Report", "Asset Email Notification")
                    val builder = AlertDialog.Builder(this@ClientProfileActivity)
                    builder.setTitle("Settings")
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> fetchAssetReport(ticker)
                            1 -> showEmailNotificationSettings(ticker)
                        }
                    }
                    builder.create().show()
                }
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

    private fun fetchAssetReport(ticker: String) {
        if (sessionToken.isEmpty() || clientId.isEmpty() || ticker.isEmpty()) {
            Toast.makeText(this, "Missing data for asset report.", Toast.LENGTH_SHORT).show()
            Log.e("AssetReport", "Missing data: sessionToken=$sessionToken, client_id=$clientId, ticker=$ticker")
            return
        }

        val request = AssetReportRequest(
            session_token = sessionToken,
            market = if (buttonStock.isSelected) "stocks" else "crypto",
            client_id = clientId,
            ticker = ticker
        )

        Log.d("AssetReport", "Request Payload: $request")

        RetrofitClient.apiService.getAssetReport(request).enqueue(object : Callback<AssetReportResponse> {
            override fun onResponse(call: Call<AssetReportResponse>, response: Response<AssetReportResponse>) {
                if (response.isSuccessful) {
                    val report = response.body()
                    if (report != null && report.status == "Success") {
                        showAssetReportDialog(report, ticker)
                    } else {
                        val errorMessage = report?.status ?: "Unknown error"
                        Toast.makeText(this@ClientProfileActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AssetReport", "HTTP Error: ${response.code()}, $errorBody")
                    Toast.makeText(this@ClientProfileActivity, "Failed to fetch report. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AssetReportResponse>, t: Throwable) {
                Log.e("AssetReport", "API failure: ${t.message}")
                Toast.makeText(this@ClientProfileActivity, "Error connecting to server. Try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAssetReportDialog(report: AssetReportResponse, ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_asset_report, null)

        val tickerTextView = dialogView.findViewById<TextView>(R.id.tickerTextView)
        val profitTextView = dialogView.findViewById<TextView>(R.id.profitTextView)
        val totalInvestedTextView = dialogView.findViewById<TextView>(R.id.totalInvestedTextView)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        // Populate the dialog with report data
        tickerTextView.text = "Ticker: $ticker"
        profitTextView.text = "Profit: $%.2f".format(report.profit)
        totalInvestedTextView.text = "Total Invested: $%.2f".format(report.total_usd_invested)

        // Create and show the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEmailNotificationSettings(ticker: String) {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_price_alert, null)
        val tickerTextView: TextView = dialogView.findViewById(R.id.tickerTextView)
        val priceInput: EditText = dialogView.findViewById(R.id.priceInput)
        val setAlertButton: Button = dialogView.findViewById(R.id.setAlertButton)

        // Set the ticker in the TextView
        tickerTextView.text = "Ticker: $ticker"

        // Create and show the dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Price Alert")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        setAlertButton.setOnClickListener {
            val price = priceInput.text.toString().toDoubleOrNull()

            if (price != null && price > 0) {
                dialog.dismiss()
                setPriceAlert(ticker, price)
            } else {
                Toast.makeText(this, "Please enter a valid price.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setPriceAlert(ticker: String, price: Double) {
        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the API request
        val request = SetPriceAlertRequest(
            session_token = sessionToken,
            ticker = ticker,
            price = price,
            market = if (buttonStock.isSelected) "stocks" else "crypto"
        )

        // Send the request
        RetrofitClient.apiService.createPriceAlert(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@ClientProfileActivity, "Price alert set successfully for $ticker!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PriceAlert", "Error: $errorBody")
                    Toast.makeText(this@ClientProfileActivity, "Failed to set price alert. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("PriceAlert", "API Failure: ${t.message}")
                Toast.makeText(this@ClientProfileActivity, "Failed to connect. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
