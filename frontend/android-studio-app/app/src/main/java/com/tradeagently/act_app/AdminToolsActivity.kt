package com.tradeagently.act_app

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminToolsActivity : AppCompatActivity() {

    private lateinit var ticketButton: Button
    private lateinit var reviewButton: Button
    private lateinit var usersButton: Button
    private lateinit var filterOpenButton: Button
    private lateinit var filterClosedButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminToolsAdapter
    private var sessionToken: String? = null
    private var ticketFilter: String = "OPEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_tools)
        NavigationHelper.setupBottomNavigation(this, -1)

        ticketButton = findViewById(R.id.buttonTicket)
        reviewButton = findViewById(R.id.buttonReview)
        usersButton = findViewById(R.id.buttonUsers)
        filterOpenButton = findViewById(R.id.buttonFilterOpen)
        filterClosedButton = findViewById(R.id.buttonFilterClosed)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", null)

        setButtonSelected(ticketButton, true)
        setFilterButtonSelected(filterOpenButton, true)
        showFilterButtons(true)
        fetchSupportTickets()

        ticketButton.setOnClickListener {
            setButtonSelected(ticketButton, true)
            showFilterButtons(true)
            fetchSupportTickets()
        }
        reviewButton.setOnClickListener {
            setButtonSelected(reviewButton, true)
            showFilterButtons(false)
            fetchReviews()
        }
        usersButton.setOnClickListener {
            setButtonSelected(usersButton, true)
            showFilterButtons(false)
            fetchUsers()
        }

        filterOpenButton.setOnClickListener {
            ticketFilter = "OPEN"
            setFilterButtonSelected(filterOpenButton, true)
            fetchSupportTickets()
        }

        filterClosedButton.setOnClickListener {
            ticketFilter = "RESOLVED"
            setFilterButtonSelected(filterClosedButton, true)
            fetchSupportTickets()
        }
    }

    private fun setButtonSelected(button: Button, isSelected: Boolean) {
        ticketButton.isSelected = false
        reviewButton.isSelected = false
        usersButton.isSelected = false
        button.isSelected = isSelected
    }

    private fun showFilterButtons(show: Boolean) {
        if (show) {
            filterOpenButton.visibility = Button.VISIBLE
            filterClosedButton.visibility = Button.VISIBLE
        } else {
            filterOpenButton.visibility = Button.GONE
            filterClosedButton.visibility = Button.GONE
        }
    }

    private fun setFilterButtonSelected(button: Button, isClicked: Boolean) {
        filterOpenButton.isSelected = false
        filterClosedButton.isSelected = false
        button.isSelected = isClicked
    }

    private fun fetchSupportTickets() {
        sessionToken?.let { token ->
            RetrofitClient.apiService.getSupportTicketList(TokenRequest(token)).enqueue(object : Callback<SupportTicketResponse> {
                override fun onResponse(call: Call<SupportTicketResponse>, response: Response<SupportTicketResponse>) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val allTickets = response.body()?.support_tickets ?: emptyList()
                        val filteredTickets = when (ticketFilter) {
                            "OPEN" -> allTickets.filter { it.issue_status.equals("OPEN", ignoreCase = true) }
                            "RESOLVED" -> allTickets.filter { it.issue_status.equals("RESOLVED", ignoreCase = true) }
                            else -> allTickets
                        }

                        adapter = AdminToolsAdapter(tickets = filteredTickets, context = this@AdminToolsActivity)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@AdminToolsActivity, "Failed to fetch tickets", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SupportTicketResponse>, t: Throwable) {
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
                        adapter = AdminToolsAdapter(reviews = reviews, context = this@AdminToolsActivity)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@AdminToolsActivity, "Failed to fetch reviews", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReviewListResponse>, t: Throwable) {
                    Toast.makeText(this@AdminToolsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchUsers() {
        sessionToken?.let { token ->
            RetrofitClient.apiService.getUserList(TokenRequest(token)).enqueue(object : Callback<UserListResponse> {
                override fun onResponse(call: Call<UserListResponse>, response: Response<UserListResponse>) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        val users = response.body()?.user_list?.filter { it.user_type != "admin" } ?: emptyList()
                        adapter = AdminToolsAdapter(users = users, context = this@AdminToolsActivity) { userId ->
                            confirmUserDeletion(userId)
                        }
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@AdminToolsActivity, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserListResponse>, t: Throwable) {
                    Toast.makeText(this@AdminToolsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun confirmUserDeletion(userId: String) {
            AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes") { _, _ -> deleteUser(userId) }
                .setNegativeButton("No", null)
                .show()
        }

    private fun deleteUser(userId: String) {
        sessionToken?.let { token ->
            RetrofitClient.apiService.adminDeleteUser(AdminDeleteUserRequest(token, userId)).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        Toast.makeText(this@AdminToolsActivity, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        fetchUsers()
                    } else {
                        Toast.makeText(this@AdminToolsActivity, "Failed to delete user", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@AdminToolsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
