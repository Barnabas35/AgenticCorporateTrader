package com.tradeagently.act_app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssetReportActivity : AppCompatActivity() {

    private lateinit var sessionToken: String
    private lateinit var ticker: String
    private lateinit var market: String
    private lateinit var clientId: String

    private lateinit var tickerTextView: TextView
    private lateinit var totalInvestedTextView: TextView
    private lateinit var profitTextView: TextView
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asset_report)

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize views
        tickerTextView = findViewById(R.id.tickerTextView)
        totalInvestedTextView = findViewById(R.id.totalInvestedTextView)
        profitTextView = findViewById(R.id.profitTextView)
        statusTextView = findViewById(R.id.statusTextView)

        // Retrieve data from intent
        sessionToken = intent.getStringExtra("session_token") ?: ""
        ticker = intent.getStringExtra("ticker") ?: ""
        market = intent.getStringExtra("market") ?: ""
        clientId = intent.getStringExtra("client_id") ?: ""

        // Display the ticker name
        tickerTextView.text = ticker

        if (sessionToken.isNotEmpty() && ticker.isNotEmpty() && market.isNotEmpty() && clientId.isNotEmpty()) {
            fetchAssetReport()
        } else {
            Toast.makeText(this, "Missing required data to generate report.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchAssetReport() {
        val request = AssetReportRequest(
            session_token = sessionToken,
            market = market,
            client_id = clientId,
            ticker_symbol = ticker
        )

        RetrofitClient.apiService.getAssetReport(request).enqueue(object : Callback<AssetReportResponse> {
            override fun onResponse(call: Call<AssetReportResponse>, response: Response<AssetReportResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val report = response.body()
                    displayReport(report)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AssetReport", "Error: $errorBody")
                    Toast.makeText(this@AssetReportActivity, "Failed to fetch asset report.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AssetReportResponse>, t: Throwable) {
                Log.e("AssetReport", "API Failure: ${t.message}")
                Toast.makeText(this@AssetReportActivity, "Failed to connect. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayReport(report: AssetReportResponse?) {
        report?.let {
            totalInvestedTextView.text = "Total Invested: $%.2f".format(it.total_usd_invested)
            profitTextView.text = "Profit: $%.2f".format(it.profit)
            statusTextView.text = "Status: ${it.status}"
        }
    }
}
