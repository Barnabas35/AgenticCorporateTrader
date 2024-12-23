package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

        NavigationHelper.setupBottomNavigation(this, R.id.nav_my_assets)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sessionToken = prefs.getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewAssets)
        userBalanceTextView = findViewById(R.id.userBalance)
        addBalanceButton = findViewById(R.id.addBalanceButton)
        buttonStock = findViewById(R.id.buttonStock)
        buttonCrypto = findViewById(R.id.buttonCrypto)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchUserBalance()
        fetchUserType()

        addBalanceButton.setOnClickListener {
            navigateToAddBalance()
        }

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
            override fun onResponse(
                call: Call<UserTypeResponse>,
                response: Response<UserTypeResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    userType = response.body()?.user_type ?: ""
                    setupUI()
                    fetchInitialData()
                } else {
                    logApiError(response)
                    Toast.makeText(
                        this@MyAssetsActivity,
                        "Failed to determine user type.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching user type: ${t.message}")
                Toast.makeText(
                    this@MyAssetsActivity,
                    "Error fetching user type.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupUI() {
        val buttonContainer: View = findViewById(R.id.buttonContainer)
        if (userType == "fm") {
            buttonContainer.visibility = View.GONE
        } else {
            buttonContainer.visibility = View.VISIBLE
            setButtonSelected(buttonStock, true)
            setButtonSelected(buttonCrypto, false)
        }
    }

    private fun fetchInitialData() {
        when (userType) {
            "fm" -> fetchClientList()
            "fa" -> fetchClientListAndSelectFirstClient()
            else -> fetchUserAssetsForSelf()
        }
    }

    private fun setButtonSelected(button: Button, isSelected: Boolean) {
        button.isSelected = isSelected
        button.setTextColor(resources.getColor(android.R.color.white))
    }

    private fun fetchUserBalance() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getBalance(request).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(
                call: Call<BalanceResponse>,
                response: Response<BalanceResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val balance = response.body()?.balance ?: 0.0
                    userBalanceTextView.text = "Balance: $%.2f".format(balance)
                } else {
                    logApiError(response)
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error fetching balance: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error fetching balance.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun fetchClientList() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getClientList(request)
            .enqueue(object : Callback<ClientListResponse> {
                override fun onResponse(
                    call: Call<ClientListResponse>,
                    response: Response<ClientListResponse>
                ) {
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
        RetrofitClient.apiService.getClientList(request)
            .enqueue(object : Callback<ClientListResponse> {
                override fun onResponse(
                    call: Call<ClientListResponse>,
                    response: Response<ClientListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val clients = response.body()?.clients ?: listOf()
                        val defaultClient = clients.firstOrNull()
                        if (defaultClient != null) {
                            client_id = defaultClient.client_id
                            client_name = defaultClient.client_name
                            fetchUserAssetsForSelf()
                        } else {
                            Toast.makeText(
                                this@MyAssetsActivity,
                                "No clients available.",
                                Toast.LENGTH_SHORT
                            ).show()
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

        RetrofitClient.apiService.getUserAssets(request)
            .enqueue(object : Callback<UserAssetsResponse> {
                override fun onResponse(
                    call: Call<UserAssetsResponse>,
                    response: Response<UserAssetsResponse>
                ) {
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
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.client_item, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val clientName: TextView = holder.itemView.findViewById(R.id.clientName)
                clientName.text = displayableClients[position]

                holder.itemView.setOnClickListener {
                    val selectedClient = clients[position]

                    val intent = Intent(this@MyAssetsActivity, ClientProfileActivity::class.java)
                    intent.putExtra("client_id", selectedClient.client_id)
                    intent.putExtra("client_name", selectedClient.client_name)
                    intent.putExtra("myUserClientId", selectedClient.client_id)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }

            override fun getItemCount() = displayableClients.size
        }

        recyclerView.adapter = adapter
    }

    private fun updateRecyclerViewWithAssets(assets: List<String>) {
        displayableAssets = assets
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

                val ticker = displayableAssets[position]
                val market = if (buttonStock.isSelected) "stocks" else "crypto"

                assetName.text = ticker

                val request = GetAssetRequest(
                    session_token = sessionToken,
                    market = market,
                    ticker = ticker,
                    client_id = client_id
                )
                RetrofitClient.apiService.getAsset(request)
                    .enqueue(object : Callback<AssetResponse> {
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

                holder.itemView.setOnClickListener {
                    if (market == "stocks") {
                        fetchTickerInfoAndOpenStockProfile(ticker)
                    } else {
                        fetchCryptoInfoAndOpenCryptoProfile(ticker)
                    }
                }

                buyButton.setOnClickListener {
                    openBuyDialog(ticker)
                }

                sellButton.setOnClickListener {
                    openSellDialog(ticker)
                }

                settingsButton.setOnClickListener {
                    val options = arrayOf(
                        "Asset Report",
                        "Asset Email Notification",
                        "Asset Accounting"
                    )
                    val builder = AlertDialog.Builder(this@MyAssetsActivity)
                    builder.setTitle("More")
                    builder.setItems(options) { _, which ->
                        when (which) {
                            0 -> fetchAssetReport(ticker)
                            1 -> showEmailNotificationSettings(ticker)
                            2 -> fetchAiAccounting(ticker)
                        }
                    }
                    builder.create().show()
                }
            }

            override fun getItemCount() = displayableAssets.size
        }

        recyclerView.adapter = adapter
    }

    private fun fetchTickerInfoAndOpenStockProfile(ticker: String) {
        val tickerRequest = TickerRequest(ticker = ticker, session_token = sessionToken)
        RetrofitClient.apiService.getTickerInfo(tickerRequest).enqueue(object : Callback<TickerInfoResponse> {
            override fun onResponse(call: Call<TickerInfoResponse>, response: Response<TickerInfoResponse>) {
                if (response.isSuccessful && response.body()?.ticker_info != null) {
                    val tickerInfo = response.body()?.ticker_info!!
                    val intent = Intent(this@MyAssetsActivity, StockProfileActivity::class.java).apply {
                        putExtra("company_name", tickerInfo.company_name)
                        putExtra("symbol", tickerInfo.symbol)
                        putExtra("close_price", tickerInfo.close_price)
                        putExtra("change_percentage", tickerInfo.change_percentage)
                        putExtra("company_description", tickerInfo.company_description)
                        putExtra("high_price", tickerInfo.high_price)
                        putExtra("low_price", tickerInfo.low_price)
                        putExtra("open_price", tickerInfo.open_price)
                        putExtra("volume", tickerInfo.volume)
                        putExtra("currency", tickerInfo.currency)
                        putExtra("homepage", tickerInfo.homepage)
                    }
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                } else {
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch stock info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TickerInfoResponse>, t: Throwable) {
                Toast.makeText(this@MyAssetsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("MyAssetsActivity", "Failed to fetch stock info", t)
            }
        })
    }

    private fun fetchCryptoInfoAndOpenCryptoProfile(ticker: String) {
        val cryptoRequest = CryptoInfoRequest(crypto = ticker, session_token = sessionToken)
        RetrofitClient.apiService.getCryptoInfo(cryptoRequest).enqueue(object : Callback<CryptoInfoResponse> {
            override fun onResponse(call: Call<CryptoInfoResponse>, response: Response<CryptoInfoResponse>) {
                if (response.isSuccessful && response.body()?.crypto_info != null) {
                    val cryptoInfo = response.body()?.crypto_info!!
                    val intent = Intent(this@MyAssetsActivity, CryptoProfileActivity::class.java).apply {
                        putExtra("symbol", cryptoInfo.symbol)
                        putExtra("name", cryptoInfo.name)
                        putExtra("latest_price", cryptoInfo.latest_price)
                        putExtra("description", cryptoInfo.description)
                        putExtra("high", cryptoInfo.high)
                        putExtra("low", cryptoInfo.low)
                        putExtra("volume", cryptoInfo.volume)
                        putExtra("open", cryptoInfo.open)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch crypto info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CryptoInfoResponse>, t: Throwable) {
                Toast.makeText(this@MyAssetsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("MyAssetsActivity", "Failed to fetch crypto info", t)
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

    private fun fetchAiAccounting(ticker: String) {
        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session token missing. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val market = if (buttonStock.isSelected) "stocks" else "crypto"
        val request = AiAccountingRequest(
            session_token = sessionToken,
            market = market,
            ticker = ticker,
            client_id = client_id
        )

        RetrofitClient.apiService.getAiAccounting(request).enqueue(object : Callback<AiAccountingResponse> {
            override fun onResponse(call: Call<AiAccountingResponse>, response: Response<AiAccountingResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val accResp = response.body()!!
                    showAiAccountingDialog(ticker, accResp)
                } else {
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch AI accounting.", Toast.LENGTH_SHORT).show()
                    val errorBody = response.errorBody()?.string()
                    Log.e("AIAccounting", "HTTP Error: ${response.code()}, $errorBody")
                }
            }

            override fun onFailure(call: Call<AiAccountingResponse>, t: Throwable) {
                Log.e("AIAccounting", "API Failure: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error fetching AI accounting data.", Toast.LENGTH_SHORT).show()
            }
        })
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
                    Toast.makeText(
                        this@MyAssetsActivity,
                        "Successfully bought $ticker!",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchUserAssetsForSelf()
                } else {
                    logApiError(response)
                    Toast.makeText(
                        this@MyAssetsActivity,
                        "Failed to buy $ticker.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error buying asset: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error buying asset.", Toast.LENGTH_SHORT)
                    .show()
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
                    Toast.makeText(
                        this@MyAssetsActivity,
                        "Successfully sold $ticker!",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchUserAssetsForSelf()
                } else {
                    logApiError(response)
                    Toast.makeText(
                        this@MyAssetsActivity,
                        "Failed to sell $ticker.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_ERROR", "Error selling asset: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error selling asset.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun fetchAssetReport(ticker: String) {
        if (sessionToken.isEmpty() || client_id.isEmpty() || ticker.isEmpty()) {
            Toast.makeText(this, "Missing data for asset report.", Toast.LENGTH_SHORT).show()
            Log.e("AssetReport", "Missing data: sessionToken=$sessionToken, client_id=$client_id, ticker=$ticker")
            return
        }

        val request = AssetReportRequest(
            session_token = sessionToken,
            market = if (buttonStock.isSelected) "stocks" else "crypto",
            client_id = client_id,
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
                        Toast.makeText(this@MyAssetsActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AssetReport", "HTTP Error: ${response.code()}, $errorBody")
                    Toast.makeText(this@MyAssetsActivity, "Failed to fetch report. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AssetReportResponse>, t: Throwable) {
                Log.e("AssetReport", "API failure: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Error connecting to server. Try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAssetReportDialog(report: AssetReportResponse, ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_asset_report, null)

        val tickerTextView = dialogView.findViewById<TextView>(R.id.tickerTextView)
        val profitTextView = dialogView.findViewById<TextView>(R.id.profitTextView)
        val totalInvestedTextView = dialogView.findViewById<TextView>(R.id.totalInvestedTextView)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        tickerTextView.text = "Ticker: $ticker"
        profitTextView.text = "Profit: $%.2f".format(report.profit)
        totalInvestedTextView.text = "Total Invested: $%.2f".format(report.total_usd_invested)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Asset Report")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEmailNotificationSettings(ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_price_alert, null)
        val tickerTextView: TextView = dialogView.findViewById(R.id.tickerTextView)
        val priceInput: EditText = dialogView.findViewById(R.id.priceInput)
        val setAlertButton: Button = dialogView.findViewById(R.id.setAlertButton)

        tickerTextView.text = "Ticker: $ticker"

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

        val request = SetPriceAlertRequest(
            session_token = sessionToken,
            ticker = ticker,
            price = price,
            market = if (buttonStock.isSelected) "stocks" else "crypto"
        )

        RetrofitClient.apiService.createPriceAlert(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@MyAssetsActivity, "Price alert set successfully for $ticker!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PriceAlert", "Error: $errorBody")
                    Toast.makeText(this@MyAssetsActivity, "Failed to set price alert. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("PriceAlert", "API Failure: ${t.message}")
                Toast.makeText(this@MyAssetsActivity, "Failed to connect. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAiAccountingDialog(ticker: String, accResp: AiAccountingResponse) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ai_accounting, null)
        val tickerTextView: TextView = dialogView.findViewById(R.id.tickerTextView)
        val growthTextView: TextView = dialogView.findViewById(R.id.growthTextView)
        val liquidityTextView: TextView = dialogView.findViewById(R.id.liquidityTextView)
        val profitabilityTextView: TextView = dialogView.findViewById(R.id.profitabilityTextView)
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        tickerTextView.text = "Ticker: $ticker"
        growthTextView.text = accResp.asset_growth
        liquidityTextView.text = accResp.asset_liquidity
        profitabilityTextView.text = accResp.asset_profitability

        val dialog = AlertDialog.Builder(this)
            .setTitle("Asset Accounting")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

