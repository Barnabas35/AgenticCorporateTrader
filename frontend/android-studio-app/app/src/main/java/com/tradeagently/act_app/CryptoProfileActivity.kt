package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLUE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CryptoProfileActivity : AppCompatActivity() {

    private lateinit var symbolTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var latestPriceTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var highPriceTextView: TextView
    private lateinit var lowPriceTextView: TextView
    private lateinit var volumeTextView: TextView
    private lateinit var openPriceTextView: TextView
    private lateinit var lineChart: LineChart
    private lateinit var timeframeSpinner: Spinner
    private lateinit var searchButton: Button
    private lateinit var descriptionCryptoButton: Button
    private lateinit var aiForecastCryptoButton: Button
    private lateinit var aiForecastResponseTextView: TextView
    private lateinit var loadingOverlay: FrameLayout
    private var userType: String = ""
    private var clientId: String = ""
    private var clients: List<Client> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto_profile)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        NavigationHelper.setupBottomNavigation(this, -1)
        initializeViews()
        fetchUserDetails()
        displayCryptoInfoFromIntent()
        fetchDefaultAggregates()
        val buyButton: Button = findViewById(R.id.buyCryptoButton)
        buyButton.setOnClickListener {
            val ticker = intent.getStringExtra("symbol") ?: "AAPL"
            openBuyDialog(ticker)
        }
    }

    private fun initializeViews() {
        symbolTextView = findViewById(R.id.symbolTextView)
        nameTextView = findViewById(R.id.nameTextView)
        latestPriceTextView = findViewById(R.id.latestPriceTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        highPriceTextView = findViewById(R.id.highPriceTextView)
        lowPriceTextView = findViewById(R.id.lowPriceTextView)
        volumeTextView = findViewById(R.id.volumeTextView)
        openPriceTextView = findViewById(R.id.openPriceTextView)
        lineChart = findViewById(R.id.cryptoLineChart)
        timeframeSpinner = findViewById(R.id.timeframeSpinner)
        searchButton = findViewById(R.id.searchButton)
        descriptionCryptoButton = findViewById(R.id.descriptionCrypto)
        aiForecastCryptoButton = findViewById(R.id.aiForecastCrypto)
        aiForecastResponseTextView = findViewById(R.id.aiForecastResponseTextView)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        val options = listOf("Last Hour", "Last Day", "Last Week", "Last Month", "Last Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeframeSpinner.adapter = adapter
        timeframeSpinner.setSelection(1)
        showDescription()
        toggleButtonColors(descriptionCryptoButton, aiForecastCryptoButton)
        descriptionCryptoButton.setOnClickListener {
            showDescription()
            toggleButtonColors(descriptionCryptoButton, aiForecastCryptoButton)
        }
        aiForecastCryptoButton.setOnClickListener {
            showLoadingOverlay()
            descriptionTextView.visibility = View.GONE
            val ticker = intent.getStringExtra("symbol") ?: "BTC"
            fetchAiAssetReport(ticker)
            toggleButtonColors(aiForecastCryptoButton, descriptionCryptoButton)
        }
        searchButton.setOnClickListener {
            val selectedTimeframe = timeframeSpinner.selectedItem.toString()
            applyTimeframeFilter(selectedTimeframe)
        }
    }

    private fun fetchUserDetails() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        if (sessionToken.isNullOrEmpty()) {
            Log.w("CryptoProfileActivity", "No session token found. userType remains unknown.")
            return
        }
        val userTypeRequest = TokenRequest(sessionToken)
        RetrofitClient.apiService.getUserType(userTypeRequest).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    userType = response.body()?.user_type.toString()
                    Log.d("CryptoProfileActivity", "User type: $userType")
                    if (userType == "fa" || userType == "fm") {
                        fetchClientId(sessionToken)
                    }
                } else {
                    Log.e("CryptoProfileActivity", "Failed to fetch user type: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Log.e("CryptoProfileActivity", "Error fetching user type: ${t.message}")
            }
        })
    }

    private fun fetchClientId(sessionToken: String) {
        RetrofitClient.apiService.getClientList(TokenRequest(sessionToken)).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val c = response.body()?.clients
                    if (!c.isNullOrEmpty()) {
                        clientId = c.first().client_id
                        Log.d("CryptoProfileActivity", "Fetched clientId: $clientId")
                    } else {
                        Log.w("CryptoProfileActivity", "No clients found for this account.")
                    }
                } else {
                    Log.e("CryptoProfileActivity", "Failed to fetch client list: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("CryptoProfileActivity", "Error fetching client list: ${t.message}")
            }
        })
    }

    private fun showLoadingOverlay() {
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay() {
        loadingOverlay.visibility = View.GONE
    }

    private fun displayNoSubscriptionMessage() {
        aiForecastResponseTextView.text = "No Subscription. Please go to AI Subscription to unlock this feature."
        aiForecastResponseTextView.visibility = View.VISIBLE
    }

    private fun displayCryptoInfoFromIntent() {
        val symbol = intent.getStringExtra("symbol") ?: "BTC"
        val name = intent.getStringExtra("name") ?: "Bitcoin"
        val latestPrice = intent.getDoubleExtra("latest_price", 0.0)
        val description = intent.getStringExtra("description") ?: "No description available"
        val high = intent.getDoubleExtra("high", 0.0)
        val low = intent.getDoubleExtra("low", 0.0)
        val volume = intent.getLongExtra("volume", 0)
        val open = intent.getDoubleExtra("open", 0.0)
        symbolTextView.text = symbol
        nameTextView.text = name
        latestPriceTextView.text = "Latest Price: ${String.format("%.6f", latestPrice)}"
        descriptionTextView.text = description
        highPriceTextView.text = "High: ${String.format("%.6f", high)}"
        lowPriceTextView.text = "Low: ${String.format("%.6f", low)}"
        volumeTextView.text = "Volume: $volume"
        openPriceTextView.text = "Open: ${String.format("%.6f", open)}"
    }

    private fun fetchAiAssetReport(ticker: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        if (sessionToken.isNullOrEmpty()) {
            hideLoadingOverlay()
            displayNoSubscriptionMessage()
            return
        }
        aiForecastCryptoButton.isEnabled = false
        Log.d("CryptoProfileActivity", "fetchAiAssetReport -> START for $ticker")
        val request = AiAssetReportRequest(session_token = sessionToken, market = "crypto", ticker = ticker)
        RetrofitClient.apiService.getAiAssetReport(request).enqueue(object : Callback<AiAssetReportResponse> {
            override fun onResponse(call: Call<AiAssetReportResponse>, response: Response<AiAssetReportResponse>) {
                aiForecastCryptoButton.isEnabled = true
                hideLoadingOverlay()
                if (response.isSuccessful) {
                    val aiBody = response.body()
                    if (aiBody == null) {
                        Toast.makeText(this@CryptoProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (aiBody.status.equals("No active subscription.", ignoreCase = true)) {
                        displayNoSubscriptionMessage()
                        return
                    } else if (!aiBody.status.equals("success", ignoreCase = true)) {
                        Toast.makeText(this@CryptoProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val future = aiBody.future ?: "No future prediction"
                    val recommend = aiBody.recommend ?: "No recommendation"
                    val mainText = aiBody.response
                    val forecastReport = """
                        Prediction: $future
                        Recommendation: $recommend

                        $mainText
                    """.trimIndent()
                    aiForecastResponseTextView.text = forecastReport
                    aiForecastResponseTextView.visibility = View.VISIBLE
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    if (errorBody.contains("No active subscription.", ignoreCase = true)) {
                        displayNoSubscriptionMessage()
                    } else {
                        Toast.makeText(this@CryptoProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("CryptoProfileActivity", "fetchAiAssetReport -> FAIL: $errorBody")
                }
            }
            override fun onFailure(call: Call<AiAssetReportResponse>, t: Throwable) {
                aiForecastCryptoButton.isEnabled = true
                hideLoadingOverlay()
                Log.e("CryptoProfileActivity", "fetchAiAssetReport -> ERROR: ${t.message}")
                Toast.makeText(this@CryptoProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDescription() {
        aiForecastResponseTextView.visibility = View.GONE
        descriptionTextView.visibility = View.VISIBLE
    }

    private fun toggleButtonColors(selectedButton: Button, otherButton: Button) {
        selectedButton.setBackgroundResource(R.drawable.btn_bg)
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4caf50"))
        selectedButton.setTextColor(Color.WHITE)
        otherButton.setBackgroundResource(R.drawable.btn_bg)
        otherButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#333333"))
        otherButton.setTextColor(Color.WHITE)
    }

    private fun fetchDefaultAggregates() {
        applyTimeframeFilter("Last Day")
    }

    private fun applyTimeframeFilter(timeframe: String) {
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endDate = dateFormat.format(now.time)
        val (startCalendar, interval) = when (timeframe) {
            "Last Hour" -> {
                now.add(Calendar.DAY_OF_YEAR, -1)
                Pair(now, "1m")
            }
            "Last Day" -> {
                now.add(Calendar.DAY_OF_YEAR, -1)
                Pair(now, "1h")
            }
            "Last Week" -> {
                now.add(Calendar.DAY_OF_YEAR, -7)
                Pair(now, "1d")
            }
            "Last Month" -> {
                now.add(Calendar.MONTH, -1)
                Pair(now, "1d")
            }
            "Last Year" -> {
                now.add(Calendar.YEAR, -1)
                Pair(now, "1wk")
            }
            else -> {
                now.add(Calendar.DAY_OF_YEAR, -1)
                Pair(now, "1h")
            }
        }
        val startDate = dateFormat.format(startCalendar.time)
        fetchCryptoAggregates(startDate, endDate, interval)
    }

    private fun fetchCryptoAggregates(
        startDate: String,
        endDate: String,
        interval: String
    ) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        val cryptoSymbol = intent.getStringExtra("symbol") ?: "BTC"
        if (sessionToken == null) {
            Log.e("CryptoProfileActivity", "Session token is missing. Not fetching aggregates.")
            lineChart.setNoDataText("Please log in to view this data.")
            return
        }
        Log.d("CryptoProfileActivity", "Fetching aggregates for $cryptoSymbol from $startDate to $endDate interval=$interval")
        val request = CryptoAggregatesRequest(
            crypto = cryptoSymbol,
            session_token = sessionToken,
            start_date = startDate,
            end_date = endDate,
            interval = interval
        )
        RetrofitClient.apiService.getCryptoAggregates(request).enqueue(object : Callback<CryptoAggregatesResponse> {
            override fun onResponse(call: Call<CryptoAggregatesResponse>, response: Response<CryptoAggregatesResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    var data = response.body()?.crypto_aggregates
                    if (data.isNullOrEmpty()) {
                        lineChart.setNoDataText("No available data for this period.")
                        lineChart.invalidate()
                    } else {
                        if (interval == "1m") {
                            data = data.take(60)
                        }
                        updateChartWithData(data)
                    }
                } else {
                    lineChart.setNoDataText("Error fetching data.")
                    lineChart.invalidate()
                }
            }
            override fun onFailure(call: Call<CryptoAggregatesResponse>, t: Throwable) {
                lineChart.setNoDataText("Failed to load data. Check your network connection.")
                lineChart.invalidate()
            }
        })
    }

    private fun updateChartWithData(aggregates: List<CryptoAggregate>) {
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        aggregates.reversed().forEachIndexed { index, aggregate ->
            val closePrice = aggregate.close.toFloat()
            val date = dateFormat.parse(aggregate.date) ?: return@forEachIndexed
            dateLabels.add(displayDateFormat.format(date))
            entries.add(Entry(index.toFloat(), closePrice))
        }
        val lineDataSet = LineDataSet(entries, "Close Price").apply {
            lineWidth = 2f
            color = BLUE
            setCircleColor(BLUE)
            circleRadius = 2f
            setDrawValues(false)
        }
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            granularity = 1f
        }
        lineChart.description.text = "Close Price Over Time"
        lineChart.invalidate()
    }

    private fun openBuyDialog(ticker: String) {
        if (userType == "fm") {
            openBuyManagerDialog(ticker)
        } else {
            openRegularBuyDialog(ticker)
        }
    }

    private fun openBuyManagerDialog(ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_buy_manager, null)
        val clientSpinner = dialogView.findViewById<Spinner>(R.id.clientSpinner)
        fetchClientList { cList ->
            this.clients = cList
            val clientNames = cList.map { it.client_name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clientSpinner.adapter = adapter
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Buy $ticker for Client")
            .setView(dialogView)
            .setPositiveButton("Buy") { _, _ ->
                val quantityInput = dialogView.findViewById<EditText>(R.id.quantityInputbuyManager)
                val quantity = quantityInput.text.toString().toDoubleOrNull()
                val selectedClient = clientSpinner.selectedItem?.toString()
                if (quantity != null && selectedClient != null) {
                    val client = clients.find { it.client_name == selectedClient }
                    if (client != null) {
                        executeBuyTransaction(ticker, quantity, client.client_id)
                    } else {
                        Toast.makeText(this, "Client not found. Please select a valid client.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid input. Please check your entries.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun openRegularBuyDialog(ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_buy, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Buy $ticker")
            .setView(dialogView)
            .setPositiveButton("Buy") { _, _ ->
                val quantityInput = dialogView.findViewById<EditText>(R.id.quantityInputbuy)
                val quantity = quantityInput.text.toString().toDoubleOrNull()
                if (quantity != null) {
                    executeBuyTransaction(ticker, quantity, clientId)
                } else {
                    Toast.makeText(this, "Invalid quantity.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun executeBuyTransaction(ticker: String, quantity: Double, clientId: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        if (sessionToken.isNullOrEmpty()) {
            Toast.makeText(this, "Session token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        val request = PurchaseAssetRequest(
            session_token = sessionToken,
            usd_quantity = quantity,
            market = "crypto",
            ticker = ticker,
            client_id = clientId
        )
        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@CryptoProfileActivity, "Successfully bought $ticker!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CryptoProfileActivity", "Error Response: $errorBody")
                    Toast.makeText(
                        this@CryptoProfileActivity,
                        "Failed to buy $ticker. Server response: $errorBody",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("CryptoProfileActivity", "API Failure: ${t.message}")
                Toast.makeText(this@CryptoProfileActivity, "Failed to connect. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchClientList(callback: (List<Client>) -> Unit) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        if (sessionToken.isNullOrEmpty()) return
        RetrofitClient.apiService.getClientList(TokenRequest(sessionToken)).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val c = response.body()?.clients ?: emptyList()
                    callback(c)
                }
            }
            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Toast.makeText(this@CryptoProfileActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
