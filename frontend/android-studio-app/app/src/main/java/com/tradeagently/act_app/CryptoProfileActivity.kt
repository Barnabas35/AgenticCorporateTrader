package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLUE
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlin.math.abs

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

    private var userType: String = ""
    private var clientId: String = ""
    private var clients: List<Client> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto_profile)
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize views
        initializeViews()

        // Display crypto information from Intent
        displayCryptoInfoFromIntent()

        // Fetch default aggregates
        fetchDefaultAggregates()

        // Setup buttons
        setupButtons()
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

        val options = listOf("Last Hour", "Last Day", "Last Week", "Last Month", "Last Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeframeSpinner.adapter = adapter
        timeframeSpinner.setSelection(1)

        // Set default visibility for description and button colors
        showDescription() // Default to showing description
        toggleButtonColors(descriptionCryptoButton, aiForecastCryptoButton)

        // Handle "Description" Button Click
        descriptionCryptoButton.setOnClickListener {
            showDescription()
            toggleButtonColors(descriptionCryptoButton, aiForecastCryptoButton)
        }

        // Handle "AI Forecast" Button Click
        aiForecastCryptoButton.setOnClickListener {
            val ticker = intent.getStringExtra("symbol") ?: "AAPL"
            fetchAiAssetReport(ticker)
            toggleButtonColors(aiForecastCryptoButton, descriptionCryptoButton)
        }
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

    private fun setupButtons() {
        descriptionCryptoButton.setOnClickListener {
            showDescription()
            toggleButtonColors(descriptionCryptoButton, aiForecastCryptoButton)
        }

        aiForecastCryptoButton.setOnClickListener {
            val ticker = intent.getStringExtra("symbol") ?: "BTC"
            fetchAiAssetReport(ticker)
            toggleButtonColors(aiForecastCryptoButton, descriptionCryptoButton)
        }
    }

    private fun fetchAiAssetReport(ticker: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)

        if (sessionToken.isNullOrEmpty()) {
            Toast.makeText(this, "Session token is missing. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        aiForecastCryptoButton.isEnabled = false

        val request = AiAssetReportRequest(session_token = sessionToken, market = "crypto", ticker = ticker)
        RetrofitClient.apiService.getAiAssetReport(request).enqueue(object : Callback<AiAssetReportResponse> {
            override fun onResponse(call: Call<AiAssetReportResponse>, response: Response<AiAssetReportResponse>) {
                aiForecastCryptoButton.isEnabled = true

                if (response.isSuccessful && response.body()?.status == "success") {
                    val aiResponse = response.body()
                    val future = aiResponse?.future ?: "No prediction available"
                    val recommend = aiResponse?.recommend ?: "No recommendation"
                    val report = aiResponse?.response ?: "No AI report available"

                    aiForecastResponseTextView.text = "$report\n\nPrediction: $future\nRecommendation: $recommend"
                    aiForecastResponseTextView.visibility = View.VISIBLE
                    descriptionTextView.visibility = View.GONE
                } else {
                    Toast.makeText(this@CryptoProfileActivity, "Failed to fetch AI Forecast.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AiAssetReportResponse>, t: Throwable) {
                aiForecastCryptoButton.isEnabled = true
                Toast.makeText(this@CryptoProfileActivity, "Error fetching AI Forecast.", Toast.LENGTH_SHORT).show()
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
        // Default to "Last Day"
        applyTimeframeFilter("Last Day")
    }

    private fun applyTimeframeFilter(timeframe: String) {
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val endDate = dateFormat.format(now.time)

        val (startCalendar, interval) = when (timeframe) {
            "Last Hour" -> {
                // For the last hour, we still request the last day's data, but use a 1m interval.
                // Adjust this logic if the backend API requires a different time window approach.
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
                // Fallback to Last Day
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
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val cryptoSymbol = intent.getStringExtra("symbol") ?: "BTC"

        if (sessionToken == null) {
            Log.e("CryptoProfileActivity", "Session token is missing. User may need to log in.")
            lineChart.setNoDataText("Please log in to view this data.")
            return
        }

        Log.d("CryptoProfileActivity", "Fetching aggregates for $cryptoSymbol from $startDate to $endDate with interval: $interval")

        val request = CryptoAggregatesRequest(
            crypto = cryptoSymbol,
            session_token = sessionToken,
            start_date = startDate,
            end_date = endDate,
            interval = interval
        )

        RetrofitClient.apiService.getCryptoAggregates(request)
            .enqueue(object : Callback<CryptoAggregatesResponse> {
                override fun onResponse(
                    call: Call<CryptoAggregatesResponse>,
                    response: Response<CryptoAggregatesResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        var data = response.body()?.crypto_aggregates
                        if (data.isNullOrEmpty()) {
                            lineChart.setNoDataText("No available data for this period. Try another timeframe.")
                            lineChart.invalidate()
                        } else {
                            // Limit the last hour to only the first 60 aggregates
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

    // Open the buy dialog for crypto
    private fun openBuyDialog(ticker: String) {
        if (userType == "fm") {
            // Open buy dialog for Fund Managers
            openBuyManagerDialog(ticker)
        } else {
            // Open regular buy dialog for other users
            openRegularBuyDialog(ticker)
        }
    }

    // Buy dialog for Fund Managers
    private fun openBuyManagerDialog(ticker: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_buy_manager, null)
        val clientSpinner = dialogView.findViewById<Spinner>(R.id.clientSpinner)

        // Populate the spinner with client names
        fetchClientList { clients ->
            this.clients = clients // Save the client list
            val clientNames = clients.map { it.client_name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clientNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            clientSpinner.adapter = adapter
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Buy $ticker for Client")
            .setView(dialogView)
            .setPositiveButton("Buy") { _, _ ->
                // Retrieve user inputs
                val quantityInput = dialogView.findViewById<EditText>(R.id.quantityInputbuyManager)
                val quantity = quantityInput.text.toString().toDoubleOrNull()
                val selectedClient = clientSpinner.selectedItem?.toString()

                if (quantity != null && selectedClient != null) {
                    // Find the selected client and proceed with the transaction
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

    // Buy dialog for regular users
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

    // Execute the buy transaction
    private fun executeBuyTransaction(ticker: String, quantity: Double, clientId: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)

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

    // Fetch client list
    private fun fetchClientList(callback: (List<Client>) -> Unit) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)

        if (sessionToken.isNullOrEmpty()) return

        RetrofitClient.apiService.getClientList(TokenRequest(sessionToken)).enqueue(object : Callback<ClientListResponse> {
            override fun onResponse(call: Call<ClientListResponse>, response: Response<ClientListResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val clients = response.body()?.clients ?: emptyList()
                    callback(clients)
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Toast.makeText(this@CryptoProfileActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
