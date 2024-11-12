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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crypto_profile)
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize TextViews and LineChart variables
        initializeViews()

        // Display crypto information from the Intent
        displayCryptoInfoFromIntent()

        // Fetch and display crypto aggregates in the chart
        fetchCryptoAggregates()
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
        lineChart = findViewById(R.id.cryptoLineChart) // Ensure the ID matches with the layout
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

    private fun fetchCryptoAggregates() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val cryptoSymbol =
            intent.getStringExtra("symbol") ?: "BTC"  // Use symbol from Intent or a default

        if (sessionToken == null) {
            Log.e("CryptoProfileActivity", "Session token is missing. User may need to log in.")
            lineChart.setNoDataText("Please log in to view this data.")
            return
        }

        val startDate = "2024-01-01"
        val endDate = "2024-11-13"
        val interval = "1d"

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
                            Log.e(
                                "CryptoProfileActivity",
                                "No data available in crypto_aggregates."
                            )
                            lineChart.setNoDataText("No available data for this period. Try adjusting the date range.")
                            lineChart.invalidate()  // Refresh the chart to show the no-data message
                        } else {
                            Log.d("CryptoProfileActivity", "Received data: $data")
                            updateChartWithData(data)
                        }
                    } else {
                        val errorMessage = response.body()?.status ?: "Unknown error"
                        Log.e("CryptoProfileActivity", "Failed to fetch data: $errorMessage")
                        lineChart.setNoDataText("Error fetching data: $errorMessage")
                        lineChart.invalidate()
                    }
                }

                override fun onFailure(call: Call<CryptoAggregatesResponse>, t: Throwable) {
                    Log.e("CryptoProfileActivity", "Network request failed: ${t.message}")
                    lineChart.setNoDataText("Failed to load data. Please check your network connection and try again.")
                    lineChart.invalidate()
                }
            })
    }


    private fun updateChartWithData(aggregates: List<CryptoAggregate>) {
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        // Adjust date format to match the API response format exactly
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val displayDateFormat =
            SimpleDateFormat("dd/MM", Locale.getDefault()) // For displaying on the x-axis

        val reversedAggregates = aggregates.reversed()

        reversedAggregates.forEachIndexed { index, aggregate ->
            val closePrice = aggregate.close.toFloat()

            try {
                // Parse the date string to Date object
                val parsedDate = dateFormat.parse(aggregate.date)
                val formattedDate = displayDateFormat.format(parsedDate)
                dateLabels.add(formattedDate)

                entries.add(Entry(index.toFloat(), closePrice))
            } catch (e: Exception) {
                Log.e("CryptoProfileActivity", "Date parsing failed for ${aggregate.date}", e)
            }
        }

        val lineDataSet = LineDataSet(entries, "Close Price").apply {
            lineWidth = 2f
            color = BLUE
            setCircleColor(RED)
            circleRadius = 3f
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dateLabels)
            labelRotationAngle = 0f
            granularity = 1f
            setLabelCount(4, true)
            textSize = 10f
            isGranularityEnabled = true
            axisMinimum = 0f
            axisMaximum = (entries.size - 1).toFloat()
        }

        lineChart.description.text = "Close Price Over Time"
        lineChart.invalidate()
    }
}
