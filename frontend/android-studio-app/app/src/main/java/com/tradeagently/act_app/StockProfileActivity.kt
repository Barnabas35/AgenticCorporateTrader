package com.tradeagently.act_app

import android.content.Context
import android.graphics.Color.BLUE
import android.graphics.Color.RED
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_profile)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize TextViews and LineChart
        initializeViews()

        // Display stock information from the Intent
        displayStockInfoFromIntent()

        // Fetch and display the ticker aggregates in the chart
        fetchTickerAggregates()
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

    private fun fetchTickerAggregates() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val ticker = intent.getStringExtra("symbol") ?: "AAPL"  // Use symbol from Intent or a default

        if (sessionToken == null) {
            Log.e("StockProfileActivity", "Session token is missing. User may need to log in.")
            lineChart.setNoDataText("Please log in to view this data.")
            return
        }

        val startDate = "2024-10-01"
        val endDate = "2024-11-05"
        val interval = "day"
        val limit = 100

        val request = TickerAggregatesRequest(ticker, sessionToken, startDate, endDate, interval, limit)

        val apiService = RetrofitClient.createService(ApiService::class.java)
        apiService.getTickerAggregates(request).enqueue(object : Callback<TickerAggregatesResponse> {
            override fun onResponse(call: Call<TickerAggregatesResponse>, response: Response<TickerAggregatesResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val data = response.body()?.ticker_info
                    if (data.isNullOrEmpty()) {
                        Log.e("StockProfileActivity", "No data available in ticker_info.")
                        lineChart.setNoDataText("No chart data available.")
                    } else {
                        Log.d("StockProfileActivity", "Received data: $data")
                        updateChartWithData(data)
                    }
                } else {
                    val errorMessage = response.body()?.status ?: "Unknown error"
                    Log.e("StockProfileActivity", "Failed to fetch data: $errorMessage")
                    lineChart.setNoDataText("Error: $errorMessage")
                    if (errorMessage == "Incorrect session token.") {
                        Log.e("StockProfileActivity", "Invalid session token. Please log in again.")
                    }
                }
            }

            override fun onFailure(call: Call<TickerAggregatesResponse>, t: Throwable) {
                Log.e("StockProfileActivity", "Network request failed: ${t.message}")
                lineChart.setNoDataText("Failed to load data. Please try again.")
            }
        })
    }

    private fun updateChartWithData(aggregates: List<TickerAggregate>) {
        val entries = mutableListOf<Entry>()

        if (aggregates.isEmpty()) {
            lineChart.setNoDataText("No chart data available.")
            return
        }

        // Assign sequential indices instead of normalized timestamps
        aggregates.forEachIndexed { index, aggregate ->
            val closePrice = aggregate.close.toFloat()
            Log.d("StockProfileActivity", "Adding Entry(time index: $index, closePrice: $closePrice)")
            entries.add(Entry(index.toFloat(), closePrice))
        }

        // Set up the data set and configure the chart appearance
        val lineDataSet = LineDataSet(entries, "Close Price").apply {
            lineWidth = 2f
            color = BLUE
            setCircleColor(RED)
            circleRadius = 3f
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.description.text = "Close Price Over Time (Indexed)"
        lineChart.invalidate() // Refresh chart with new data
    }

}
