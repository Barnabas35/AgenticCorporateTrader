package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color.BLUE
import android.graphics.Color.RED
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import java.util.Date
import java.util.Locale

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
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var intervalSpinner: Spinner
    private lateinit var submitButton: Button

    private var userType: String = ""
    private var clientId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_profile)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize TextViews and LineChart
        initializeViews()

        // Fetch user type and client_id
        fetchUserDetails()

        // Add TextWatcher to format dates
        setupDateInputFormatters()

        // Display stock information from the Intent
        displayStockInfoFromIntent()

        // Fetch and display the ticker aggregates in the chart
        fetchTickerAggregates()

        // Handle user-submitted date ranges and intervals
        setupSubmitButton()

        // Handle Buy Button Click
        val buyCryptoButton: Button = findViewById(R.id.buyCryptoButton)
        buyCryptoButton.setOnClickListener {
            val ticker = intent.getStringExtra("symbol") ?: "AAPL" // Default to AAPL
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
        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        intervalSpinner = findViewById(R.id.intervalSpinner)
        submitButton = findViewById(R.id.submitButton)
    }

    private fun fetchUserDetails() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)

        if (sessionToken.isNullOrEmpty()) {
            Toast.makeText(this, "Session token is missing. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch user type
        val userTypeRequest = TokenRequest(sessionToken)
        RetrofitClient.apiService.getUserType(userTypeRequest).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    userType = response.body()?.user_type.toString()
                    Log.d("CryptoProfileActivity", "User type: $userType")

                    // Fetch client_id if user is a Fund Administrator (FA)
                    if (userType == "fa") {
                        fetchClientId(sessionToken)
                    }
                } else {
                    Toast.makeText(this@StockProfileActivity, "Failed to fetch user type.", Toast.LENGTH_SHORT).show()
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
                    val clients = response.body()?.clients
                    if (!clients.isNullOrEmpty()) {
                        clientId = clients.first().client_id // Select the first client for simplicity
                        Log.d("CryptoProfileActivity", "Client ID: $clientId")
                    } else {
                        Toast.makeText(this@StockProfileActivity, "No clients found for this account.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@StockProfileActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("CryptoProfileActivity", "Error fetching client list: ${t.message}")
            }
        })
    }

    private fun setupDateInputFormatters() {
        val dateTextWatcher = object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s.isNullOrEmpty()) return
                isUpdating = true

                val input = s.toString().replace("-", "")
                val formatted = StringBuilder()

                try {
                    if (input.length > 4) {
                        formatted.append(input.substring(0, 4)).append("-")
                        if (input.length > 6) {
                            formatted.append(input.substring(4, 6)).append("-")
                            formatted.append(input.substring(6))
                        } else {
                            formatted.append(input.substring(4))
                        }
                    } else {
                        formatted.append(input)
                    }

                    s.replace(0, s.length, formatted.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isUpdating = false
                }
            }
        }

        startDateInput.addTextChangedListener(dateTextWatcher)
        endDateInput.addTextChangedListener(dateTextWatcher)
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            val startDate = startDateInput.text.toString()
            val endDate = endDateInput.text.toString()
            val interval = intervalSpinner.selectedItem.toString()

            if (startDate.isBlank() || endDate.isBlank()) {
                Log.e("StockProfileActivity", "Start date and end date must be filled.")
                return@setOnClickListener
            }

            // Fetch data based on user input
            fetchTickerAggregates(startDate, endDate, interval)
        }
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

    private fun fetchTickerAggregates(startDate: String = "2024-11-15", endDate: String = "2024-11-17", interval: String = "hour") {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val ticker = intent.getStringExtra("symbol") ?: "AAPL" // Default to "AAPL"

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
            limit = 100 // Arbitrary limit value
        )

        val apiService = RetrofitClient.createService(ApiService::class.java)
        apiService.getTickerAggregates(request).enqueue(object : Callback<TickerAggregatesResponse> {
            override fun onResponse(
                call: Call<TickerAggregatesResponse>,
                response: Response<TickerAggregatesResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val data = response.body()?.ticker_info
                    if (data.isNullOrEmpty()) {
                        lineChart.setNoDataText("No data available for the selected range.")
                    } else {
                        updateChartWithData(data)
                    }
                } else {
                    lineChart.setNoDataText("Failed to fetch data. Try again later.")
                }
                lineChart.invalidate()
            }

            override fun onFailure(call: Call<TickerAggregatesResponse>, t: Throwable) {
                Log.e("StockProfileActivity", "Error: ${t.message}")
                lineChart.setNoDataText("Error fetching data. Check your connection.")
                lineChart.invalidate()
            }
        })
    }

    private fun updateChartWithData(aggregates: List<TickerAggregate>) {
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        // Format for the date labels
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        // Reverse the order of aggregates to display newest on the left, oldest on the right
        val reversedAggregates = aggregates.reversed()

        reversedAggregates.forEachIndexed { index, aggregate ->
            val closePrice = aggregate.close.toFloat()

            // Convert timestamp to formatted date
            val date = dateFormat.format(Date(aggregate.timestamp))
            dateLabels.add(date)

            entries.add(Entry(index.toFloat(), closePrice))
        }

        // Set up the dataset for the LineChart
        val lineDataSet = LineDataSet(entries, "Close Price").apply {
            lineWidth = 2f
            color = BLUE
            setCircleColor(RED)
            circleRadius = 3f
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Set x-axis properties to improve readability and reverse the direction
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            labelRotationAngle = 0f    // Rotate labels for readability
            granularity = 1f             // Set granularity for one label per entry
            setLabelCount(4, true)       // Display a maximum of 4 labels to avoid clutter
            textSize = 10f               // Set text size for readability
            isGranularityEnabled = true  // Enable granularity for the reversed axis
            axisMinimum = 0f             // Set the minimum for the axis
            axisMaximum = (entries.size - 1).toFloat() // Set maximum to match the entry count
        }

        lineChart.description.text = "Close Price Over Time"
        lineChart.invalidate() // Refresh the chart with new data
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

    private fun executeBuyTransaction(ticker: String, quantity: Double) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)

        if (sessionToken.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Session token is missing. Please log in again.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Ensure client_id is provided for Fund Administrators
        if (userType == "fa" && clientId.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Fund Administrator must select a client to purchase assets.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val request = PurchaseAssetRequest(
            session_token = sessionToken,
            usd_quantity = quantity,
            market = "stocks",
            ticker = ticker,
            client_id = clientId ?: "" // Empty if not required
        )

        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.status ?: "Unknown response"
                    Toast.makeText(this@StockProfileActivity, message, Toast.LENGTH_SHORT).show()

                    if (message.contains("Success", ignoreCase = true)) {
                        Log.d("CryptoProfileActivity", "Purchase successful for $ticker")
                    }
                } else {
                    Toast.makeText(
                        this@StockProfileActivity,
                        "Failed to complete purchase.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "CryptoProfileActivity",
                        "Error response: ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("CryptoProfileActivity", "API call failed: ${t.message}")
                Toast.makeText(
                    this@StockProfileActivity,
                    "Failed to connect. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
