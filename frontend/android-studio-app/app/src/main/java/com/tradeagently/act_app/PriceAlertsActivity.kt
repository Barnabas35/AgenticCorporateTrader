package com.tradeagently.act_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PriceAlertsActivity : AppCompatActivity() {

    private lateinit var sessionToken: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var noAlertsTextView: TextView
    private val priceAlertList: MutableList<PriceAlert> = mutableListOf()
    private lateinit var priceAlertAdapter: PriceAlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_alerts)

        NavigationHelper.setupBottomNavigation(this, -1)

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewPriceAlerts)
        noAlertsTextView = findViewById(R.id.noAlertsTextView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        priceAlertAdapter = PriceAlertAdapter(priceAlertList)
        recyclerView.adapter = priceAlertAdapter

        fetchPriceAlerts()
    }

    private fun fetchPriceAlerts() {
        val request = GetPriceAlertsRequest(sessionToken)
        RetrofitClient.apiService.getPriceAlerts(request).enqueue(object : Callback<GetPriceAlertsResponse> {
            override fun onResponse(call: Call<GetPriceAlertsResponse>, response: Response<GetPriceAlertsResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val alerts = response.body()?.alerts ?: emptyList()
                    priceAlertList.clear()
                    priceAlertList.addAll(alerts)
                    updateRecyclerView()
                } else {
                    priceAlertList.clear()
                    updateRecyclerView()
                }
            }

            override fun onFailure(call: Call<GetPriceAlertsResponse>, t: Throwable) {
                priceAlertList.clear()
                updateRecyclerView()
                Log.e("PriceAlertsActivity", "Error fetching price alerts: ${t.message}")
                Toast.makeText(this@PriceAlertsActivity, "Error fetching price alerts.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecyclerView() {
        Log.d("PriceAlertsActivity", "Updating RecyclerView with ${priceAlertList.size} items.")

        if (priceAlertList.isEmpty()) {
            noAlertsTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noAlertsTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            priceAlertAdapter.notifyDataSetChanged()
        }
    }

    private fun deletePriceAlert(alertId: String) {
        val request = mapOf(
            "session_token" to sessionToken,
            "alert_id" to alertId
        )

        RetrofitClient.apiService.deletePriceAlert(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@PriceAlertsActivity, "Price alert deleted successfully!", Toast.LENGTH_SHORT).show()
                    fetchPriceAlerts()
                } else {
                    Log.e("PriceAlertsActivity", "Failed to delete price alert: ${response.message()}")
                    Toast.makeText(this@PriceAlertsActivity, "Failed to delete price alert.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("PriceAlertsActivity", "Error deleting price alert: ${t.message}")
                Toast.makeText(this@PriceAlertsActivity, "Error deleting price alert.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class PriceAlertAdapter(private val alerts: List<PriceAlert>) :
        RecyclerView.Adapter<PriceAlertAdapter.PriceAlertViewHolder>() {

        inner class PriceAlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tickerTextView: TextView = view.findViewById(R.id.tickerTextView)
            val priceTextView: TextView = view.findViewById(R.id.priceTextView)
            val marketTextView: TextView = view.findViewById(R.id.marketTextView)
            val deleteButton: Button = view.findViewById(R.id.deleteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceAlertViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.price_alert_item, parent, false)
            return PriceAlertViewHolder(view)
        }

        override fun onBindViewHolder(holder: PriceAlertViewHolder, position: Int) {
            val alert = alerts[position]
            holder.tickerTextView.text = alert.ticker
            holder.priceTextView.text = "Price: $%.2f".format(alert.price)
            holder.marketTextView.text = "Market: ${alert.market}"

            holder.deleteButton.setOnClickListener {
                deletePriceAlert(alert.alert_id)
            }
        }

        override fun getItemCount(): Int = alerts.size
    }
}
