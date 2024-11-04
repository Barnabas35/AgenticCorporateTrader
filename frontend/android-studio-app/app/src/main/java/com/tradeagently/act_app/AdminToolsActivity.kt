package com.tradeagently.act_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminToolsActivity : AppCompatActivity() {

    private lateinit var ticketButton: Button
    private lateinit var reviewButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminToolsAdapter
    private var sessionToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_tools)

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy) // Use `nav_dummy` if there's no dedicated admin item

        // Initialize UI elements
        ticketButton = findViewById(R.id.buttonTicket)
        reviewButton = findViewById(R.id.buttonReview)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch session token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", null)

        // Set TICKETS as the default selected option
        setButtonSelected(ticketButton, true)
        setButtonSelected(reviewButton, false)
        fetchSupportTickets()  // Load tickets by default

        // Set up button listeners
        ticketButton.setOnClickListener {
            setButtonSelected(ticketButton, true)
            setButtonSelected(reviewButton, false)
            fetchSupportTickets()
        }
        reviewButton.setOnClickListener {
            setButtonSelected(ticketButton, false)
            setButtonSelected(reviewButton, true)
            fetchReviews()
        }
    }

    private fun setButtonSelected(button: Button, isSelected: Boolean) {
        button.isSelected = isSelected
    }

    private fun fetchSupportTickets() {
        sessionToken?.let { token ->
            RetrofitClient.apiService.getSupportTicketList(TokenRequest(token)).enqueue(object : Callback<SupportTicketResponse> {
                override fun onResponse(call: Call<SupportTicketResponse>, response: Response<SupportTicketResponse>) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val tickets = response.body()?.support_tickets ?: emptyList()
                        adapter = AdminToolsAdapter(tickets, null)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@AdminToolsActivity, "Failed to fetch support tickets", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SupportTicketResponse>, t: Throwable) {
                    Log.e("AdminToolsActivity", "Error fetching support tickets: ${t.message}")
                    Toast.makeText(this@AdminToolsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchReviews() {
        sessionToken?.let { token ->
            RetrofitClient.apiService.getReviewList(TokenRequest(token)).enqueue(object : Callback<ReviewListResponse> {
                override fun onResponse(call: Call<ReviewListResponse>, response: Response<ReviewListResponse>) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val reviews = response.body()?.reviews ?: emptyList()
                        adapter = AdminToolsAdapter(null, reviews)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@AdminToolsActivity, "Failed to fetch reviews", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReviewListResponse>, t: Throwable) {
                    Log.e("AdminToolsActivity", "Error fetching reviews: ${t.message}")
                    Toast.makeText(this@AdminToolsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
