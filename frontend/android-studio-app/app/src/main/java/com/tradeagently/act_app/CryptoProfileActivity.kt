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
import android.widget.ArrayAdapter
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
    private var clients: List<Client> = emptyList()

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
            setCircleColor(BLUE)
            circleRadius = 2f
        }

        lineChart.data = LineData(lineDataSet)
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            granularity = 1f
        }
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

        // Debug logs for confirmation
        Log.d("CryptoProfileActivity", "Executing Buy Transaction:")
        Log.d("CryptoProfileActivity", "Ticker: $ticker")
        Log.d("CryptoProfileActivity", "Quantity: $quantity")
        Log.d("CryptoProfileActivity", "Client ID: $clientId")
        Log.d("CryptoProfileActivity", "Session Token: $sessionToken")

        val request = PurchaseAssetRequest(
            session_token = sessionToken,
            usd_quantity = quantity,
            market = "crypto",
            ticker = ticker,
            client_id = clientId
        )

        RetrofitClient.apiService.purchaseAsset(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                Log.d("CryptoProfileActivity", "Response code: ${response.code()}")
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
