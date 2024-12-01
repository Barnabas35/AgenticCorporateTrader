package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color.BLUE
import android.graphics.Color.RED
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher
import android.widget.Spinner
import android.widget.Toast

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
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var intervalSpinner: Spinner
    private lateinit var submitButton: Button

    private var userType: String = ""
    private var clientId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto_profile)
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize TextViews, EditTexts, and LineChart variables
        initializeViews()

        // Fetch user type and client_id
        fetchUserDetails()

        // Add TextWatcher to format dates
        setupDateInputFormatters()

        // Display crypto information from the Intent
        displayCryptoInfoFromIntent()

        // Fetch and display crypto aggregates in the chart
        fetchCryptoAggregates()

        // Handle user-submitted date ranges and intervals
        setupSubmitButton()

        // Handle Buy Button Click
        val buyCryptoButton: Button = findViewById(R.id.buyCryptoButton)
        buyCryptoButton.setOnClickListener {
            val ticker = intent.getStringExtra("symbol") ?: "BTC" // Default to BTC
            openBuyDialog(ticker)
        }
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
                    Toast.makeText(this@CryptoProfileActivity, "Failed to fetch user type.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@CryptoProfileActivity, "No clients found for this account.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CryptoProfileActivity, "Failed to fetch client list.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ClientListResponse>, t: Throwable) {
                Log.e("CryptoProfileActivity", "Error fetching client list: ${t.message}")
            }
        })
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
        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        intervalSpinner = findViewById(R.id.intervalSpinner)
        submitButton = findViewById(R.id.submitButton)
    }


    private fun setupDateInputFormatters() {
        val dateTextWatcher = object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s.isNullOrEmpty()) return
                isUpdating = true

                val input = s.toString().replace("-", "") // Remove any existing dashes
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

                    // Update the text without resetting the cursor
                    s.replace(0, s.length, formatted.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isUpdating = false
                }
            }
        }

        // Attach the TextWatcher to both date input fields
        startDateInput.addTextChangedListener(dateTextWatcher)
        endDateInput.addTextChangedListener(dateTextWatcher)
    }


    private fun displayCryptoInfoFromIntent() {
        val symbol = intent.getStringExtra("symbol")
        val name = intent.getStringExtra("name")
        val latestPrice = intent.getDoubleExtra("latest_price", 0.0)
        val description = intent.getStringExtra("description")
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

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            val startDate = startDateInput.text.toString()
            val endDate = endDateInput.text.toString()
            val interval = intervalSpinner.selectedItem.toString()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Log.e("CryptoProfileActivity", "Start date and end date must be filled.")
                return@setOnClickListener
            }

            // Fetch and update chart with user-defined parameters
            fetchCryptoAggregates(startDate, endDate, interval)
        }
    }

    private fun fetchCryptoAggregates(
        startDate: String = "2024-11-15",
        endDate: String = "2024-11-17",
        interval: String = "1h"
    ) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val cryptoSymbol = intent.getStringExtra("symbol") ?: "BTC"

        if (sessionToken == null) {
            Log.e("CryptoProfileActivity", "Session token is missing. User may need to log in.")
            lineChart.setNoDataText("Please log in to view this data.")
            return
        }

        Log.d(
            "CryptoProfileActivity",
            "Fetching aggregates for $cryptoSymbol from $startDate to $endDate with interval: $interval"
        )

        val request = CryptoAggregatesRequest(
            crypto = cryptoSymbol,
            session_token = sessionToken,
            start_date = startDate,
            end_date = endDate,
            interval = interval
        )

        val apiService = RetrofitClient.createService(ApiService::class.java)
        apiService.getCryptoAggregates(request)
            .enqueue(object : Callback<CryptoAggregatesResponse> {
                override fun onResponse(
                    call: Call<CryptoAggregatesResponse>,
                    response: Response<CryptoAggregatesResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val data = response.body()?.crypto_aggregates
                        if (data.isNullOrEmpty()) {
                            lineChart.setNoDataText("No available data for this period. Try adjusting the date range.")
                            lineChart.invalidate()
                        } else {
                            updateChartWithData(data)
                        }
                    } else {
                        lineChart.setNoDataText("Error fetching data.")
                        lineChart.invalidate()
                    }
                }

                override fun onFailure(call: Call<CryptoAggregatesResponse>, t: Throwable) {
                    lineChart.setNoDataText("Failed to load data. Please check your network connection and try again.")
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
            setCircleColor(RED)
            circleRadius = 3f
        }

        lineChart.data = LineData(lineDataSet)
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            granularity = 1f
        }
        lineChart.invalidate()
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
            market = "crypto",
            ticker = ticker,
            client_id = clientId ?: "" // Empty if not required
        )

        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.status ?: "Unknown response"
                    Toast.makeText(this@CryptoProfileActivity, message, Toast.LENGTH_SHORT).show()

                    if (message.contains("Success", ignoreCase = true)) {
                        Log.d("CryptoProfileActivity", "Purchase successful for $ticker")
                    }
                } else {
                    Toast.makeText(
                        this@CryptoProfileActivity,
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
                    this@CryptoProfileActivity,
                    "Failed to connect. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
