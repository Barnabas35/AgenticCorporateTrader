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

class StockProfileActivity : AppCompatActivity() {

    private lateinit var companyNameTextView: TextView
    private lateinit var symbolTextView: TextView
    private lateinit var closePriceTextView: TextView
    private lateinit var changePercentageTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var highPriceTextView: TextView
    private lateinit var lowPriceTextView: TextView
    private lateinit var openPriceTextView: TextView
    private lateinit var volumeTextView: TextView
    private lateinit var homepageTextView: TextView
    private lateinit var lineChart: LineChart
    private lateinit var timeframeSpinner: Spinner
    private lateinit var searchButton: Button
    private lateinit var descriptionStockButton: Button
    private lateinit var aiForecastStockButton: Button
    private lateinit var aiForecastResponseTextView: TextView
    private lateinit var loadingOverlay: FrameLayout
    private var userType: String = ""
    private var clientId: String = ""
    private var clients: List<Client> = emptyList()
    private var currentTimeframe: String = "Last Day"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_profile)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        NavigationHelper.setupBottomNavigation(this, -1)
        initializeViews()
        fetchUserDetails()
        displayStockInfoFromIntent()
        fetchDefaultAggregates()
        val buyButton: Button = findViewById(R.id.buyStockButton)
        buyButton.setOnClickListener {
            val ticker = intent.getStringExtra("symbol") ?: "AAPL"
            openBuyDialog(ticker)
        }
    }

    private fun initializeViews() {
        companyNameTextView = findViewById(R.id.companyNameTextView)
        symbolTextView = findViewById(R.id.symbolTextView)
        closePriceTextView = findViewById(R.id.closePriceTextView)
        changePercentageTextView = findViewById(R.id.changePercentageTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        highPriceTextView = findViewById(R.id.highPriceTextView)
        lowPriceTextView = findViewById(R.id.lowPriceTextView)
        openPriceTextView = findViewById(R.id.openPriceTextView)
        volumeTextView = findViewById(R.id.volumeTextView)
        homepageTextView = findViewById(R.id.homepageTextView)
        lineChart = findViewById(R.id.lineChart)
        timeframeSpinner = findViewById(R.id.timeframeSpinner)
        searchButton = findViewById(R.id.searchButton)
        descriptionStockButton = findViewById(R.id.descriptionStock)
        aiForecastStockButton = findViewById(R.id.aiForecastStock)
        aiForecastResponseTextView = findViewById(R.id.aiForecastResponseTextView)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        val options = listOf("Last Hour", "Last Day", "Last Week", "Last Month", "Last Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeframeSpinner.adapter = adapter
        timeframeSpinner.setSelection(1)
        showDescription()
        toggleButtonColors(descriptionStockButton, aiForecastStockButton)
        descriptionStockButton.setOnClickListener {
            showDescription()
            toggleButtonColors(descriptionStockButton, aiForecastStockButton)
        }
        aiForecastStockButton.setOnClickListener {
            showLoadingOverlay()
            descriptionTextView.visibility = View.GONE
            val ticker = intent.getStringExtra("symbol") ?: "AAPL"
            fetchAiAssetReport(ticker)
            toggleButtonColors(aiForecastStockButton, descriptionStockButton)
        }
        searchButton.setOnClickListener {
            val selectedTimeframe = timeframeSpinner.selectedItem.toString()
            currentTimeframe = selectedTimeframe
            applyTimeframeFilter(selectedTimeframe)
        }
    }

    private fun fetchUserDetails() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        if (sessionToken.isNullOrEmpty()) {
            Toast.makeText(this, "Session token is missing. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getUserType(request).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    userType = response.body()?.user_type.toString()
                    Log.d("StockProfileActivity", "User type: $userType")
                    if (userType == "fa" || userType == "fm") {
                        fetchClientId(sessionToken)
                    }
                } else {
                    Toast.makeText(this@StockProfileActivity, "Failed to fetch user type.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Log.e("StockProfileActivity", "Error fetching user type: ${t.message}")
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
                        Log.d("StockProfileActivity", "Client ID: $clientId")
                    } else {
                        Toast.makeText(this@StockProfileActivity, "No clients found for this account.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@StockProfileActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("StockProfileActivity", "Error fetching client list: ${t.message}")
            }
        })
    }

    private fun displayStockInfoFromIntent() {
        val companyName = intent.getStringExtra("company_name")
        val symbol = intent.getStringExtra("symbol")
        val closePrice = intent.getDoubleExtra("close_price", 0.0)
        val changePercentage = intent.getDoubleExtra("change_percentage", 0.0)
        val description = intent.getStringExtra("company_description")
        val highPrice = intent.getDoubleExtra("high_price", 0.0)
        val lowPrice = intent.getDoubleExtra("low_price", 0.0)
        val openPrice = intent.getDoubleExtra("open_price", 0.0)
        val volume = intent.getDoubleExtra("volume", 0.0)
        val homepage = intent.getStringExtra("homepage")
        symbolTextView.text = symbol ?: "N/A"
        companyNameTextView.text = companyName ?: "N/A"
        closePriceTextView.text = "Close Price: ${String.format("%.2f", closePrice)}"
        changePercentageTextView.text = "Change Percentage: ${String.format("%.4f%%", changePercentage)}"
        descriptionTextView.text = description ?: "No description available"
        highPriceTextView.text = "High Price: ${String.format("%.2f", highPrice)}"
        lowPriceTextView.text = "Low Price: ${String.format("%.2f", lowPrice)}"
        openPriceTextView.text = "Open Price: ${String.format("%.2f", openPrice)}"
        volumeTextView.text = "Volume: ${String.format("%.0f", volume)}"
        homepageTextView.text = homepage ?: "No homepage available"
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

    private fun fetchAiAssetReport(ticker: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        if (sessionToken.isNullOrEmpty()) {
            hideLoadingOverlay()
            displayNoSubscriptionMessage()
            return
        }
        aiForecastStockButton.isEnabled = false
        Log.d("StockProfileActivity", "fetchAiAssetReport -> START for $ticker")
        val request = AiAssetReportRequest(session_token = sessionToken, market = "stocks", ticker = ticker)
        RetrofitClient.apiService.getAiAssetReport(request).enqueue(object : Callback<AiAssetReportResponse> {
            override fun onResponse(call: Call<AiAssetReportResponse>, response: Response<AiAssetReportResponse>) {
                aiForecastStockButton.isEnabled = true
                hideLoadingOverlay()
                if (response.isSuccessful) {
                    val aiBody = response.body()
                    if (aiBody == null) {
                        Toast.makeText(this@StockProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (aiBody.status.equals("No active subscription.", ignoreCase = true)) {
                        displayNoSubscriptionMessage()
                        return
                    } else if (!aiBody.status.equals("success", ignoreCase = true)) {
                        Toast.makeText(this@StockProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@StockProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("StockProfileActivity", "fetchAiAssetReport -> FAIL: $errorBody")
                }
            }
            override fun onFailure(call: Call<AiAssetReportResponse>, t: Throwable) {
                aiForecastStockButton.isEnabled = true
                hideLoadingOverlay()
                Log.e("StockProfileActivity", "fetchAiAssetReport -> ERROR: ${t.message}")
                Toast.makeText(this@StockProfileActivity, "An error has occurred.", Toast.LENGTH_SHORT).show()
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
        currentTimeframe = "Last Day"
        applyTimeframeFilter("Last Day")
    }

    private fun applyTimeframeFilter(timeframe: String) {
        currentTimeframe = timeframe
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val (startCalendar, interval) = when (timeframe) {
            "Last Hour" -> {
                now.add(Calendar.HOUR_OF_DAY, -1)
                Pair(now, "minute")
            }
            "Last Day" -> {
                now.add(Calendar.DAY_OF_YEAR, -1)
                Pair(now, "hour")
            }
            "Last Week" -> {
                now.add(Calendar.DAY_OF_YEAR, -7)
                Pair(now, "day")
            }
            "Last Month" -> {
                now.add(Calendar.MONTH, -1)
                Pair(now, "day")
            }
            "Last Year" -> {
                now.add(Calendar.YEAR, -1)
                Pair(now, "week")
            }
            else -> {
                now.add(Calendar.DAY_OF_YEAR, -1)
                Pair(now, "hour")
            }
        }
        val startDate = dateFormat.format(startCalendar.time)
        val endDate = dateFormat.format(Calendar.getInstance().time)
        fetchTickerAggregates(startDate, endDate, interval, timeframe)
    }

    private fun fetchTickerAggregates(
        startDate: String,
        endDate: String,
        interval: String,
        timeframe: String
    ) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = prefs.getString("session_token", null)
        val ticker = intent.getStringExtra("symbol") ?: "AAPL"
        if (sessionToken == null) {
            Log.e("StockProfileActivity", "Session token is missing.")
            lineChart.setNoDataText("Please log in to view this data.")
            return
        }
        val request = TickerAggregatesRequest(
            ticker = ticker,
            session_token = sessionToken,
            start_date = startDate,
            end_date = endDate,
            interval = interval,
            limit = 100
        )
        RetrofitClient.apiService.getTickerAggregates(request).enqueue(object : Callback<TickerAggregatesResponse> {
            override fun onResponse(
                call: Call<TickerAggregatesResponse>,
                response: Response<TickerAggregatesResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    var data = response.body()?.ticker_info
                    if (data.isNullOrEmpty()) {
                        lineChart.setNoDataText("No data available for the selected range.")
                        lineChart.invalidate()
                    } else {
                        if (timeframe == "Last Hour") {
                            data = filterLastHourData(data)
                        }
                        if (data.isEmpty()) {
                            lineChart.setNoDataText("No data in the last hour.")
                            lineChart.invalidate()
                        } else {
                            updateChartWithData(data)
                        }
                    }
                } else {
                    lineChart.setNoDataText("Failed to fetch data. Try again later.")
                    lineChart.invalidate()
                }
            }
            override fun onFailure(call: Call<TickerAggregatesResponse>, t: Throwable) {
                Log.e("StockProfileActivity", "Error: ${t.message}")
                lineChart.setNoDataText("Error fetching data. Check your connection.")
                lineChart.invalidate()
            }
        })
    }

    private fun filterLastHourData(aggregates: List<TickerAggregate>): List<TickerAggregate> {
        val now = System.currentTimeMillis()
        val oneHourMillis = 60 * 60 * 1000L
        return aggregates.filter { aggregate ->
            val diff = now - aggregate.timestamp
            diff in 0..oneHourMillis
        }
    }

    private fun updateChartWithData(aggregates: List<TickerAggregate>) {
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()
        val sortedAggregates = aggregates.sortedBy { it.timestamp }
        val displayHourForLastHour = currentTimeframe == "Last Hour"
        val dateFormat = if (displayHourForLastHour) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM", Locale.getDefault())
        }
        sortedAggregates.forEachIndexed { index, aggregate ->
            val closePrice = aggregate.close.toFloat()
            val date = Date(aggregate.timestamp)
            dateLabels.add(dateFormat.format(date))
            entries.add(Entry(index.toFloat(), closePrice))
        }
        val lineDataSet = LineDataSet(entries, "Close Price").apply {
            lineWidth = 2f
            color = BLUE
            setCircleColor(BLUE)
            circleRadius = 3f
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
        fetchClientList { clients ->
            this.clients = clients
            val clientNames = clients.map { it.client_name }
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
                        Toast.makeText(this, "Selected client not found.", Toast.LENGTH_SHORT).show()
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
            market = "stocks",
            ticker = ticker,
            client_id = clientId
        )
        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@StockProfileActivity, "Successfully bought $ticker!", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("StockProfileActivity", "Error Response: $errorBody")
                    Toast.makeText(
                        this@StockProfileActivity,
                        "Failed to buy $ticker. Server response: $errorBody",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("StockProfileActivity", "API Failure: ${t.message}")
                Toast.makeText(this@StockProfileActivity, "Failed to connect. Please try again.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@StockProfileActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
