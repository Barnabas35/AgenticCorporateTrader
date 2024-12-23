package com.tradeagently.act_app

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TicketDetailsActivity : AppCompatActivity() {

    private lateinit var ticketId: String
    private var sessionToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticketdetail)

        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy)

        val subjectTextView: TextView = findViewById(R.id.subjectTextView)
        val userIdTextView: TextView = findViewById(R.id.userIdTextView)
        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
        val statusTextView: TextView = findViewById(R.id.statusTextView)
        val ticketIdTextView: TextView = findViewById(R.id.ticketIdTextView)
        val timestampTextView: TextView = findViewById(R.id.timestampTextView)
        val resolveButton: Button = findViewById(R.id.saveButton)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", null)

        val issueSubject = intent.getStringExtra("issue_subject")
        val userId = intent.getStringExtra("user_id")
        val issueDescription = intent.getStringExtra("issue_description")
        val issueStatus = intent.getStringExtra("issue_status")
        ticketId = intent.getStringExtra("ticket_id") ?: ""
        val unixTimestamp = intent.getLongExtra("unix_timestamp", 0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(unixTimestamp * 1000))

        subjectTextView.text = issueSubject ?: "N/A"
        userIdTextView.text = "User ID: ${userId ?: "N/A"}"
        descriptionTextView.text = "Description: ${issueDescription ?: "N/A"}"
        statusTextView.text = "Status: ${issueStatus ?: "N/A"}"
        ticketIdTextView.text = "Ticket ID: ${ticketId}"
        timestampTextView.text = "Timestamp: $formattedDate"

        resolveButton.setOnClickListener {
            showResolveDialog()
        }
    }

    private fun showResolveDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_resolve_ticket, null)
        val subjectEditText: EditText = dialogView.findViewById(R.id.responseSubjectEditText)
        val bodyEditText: EditText = dialogView.findViewById(R.id.responseBodyEditText)

        AlertDialog.Builder(this)
            .setTitle("Resolve Ticket")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val subject = subjectEditText.text.toString().trim()
                val body = bodyEditText.text.toString().trim()

                if (subject.isNotEmpty() && body.isNotEmpty()) {
                    resolveTicket(subject, body)
                } else {
                    Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resolveTicket(subject: String, body: String) {
        sessionToken?.let { token ->
            val request = mapOf(
                "session_token" to token,
                "ticket_id" to ticketId,
                "response_subject" to subject,
                "response_body" to body
            )

            RetrofitClient.apiService.resolveSupportTicket(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.status == "Success") {
                        Toast.makeText(this@TicketDetailsActivity, "Ticket resolved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@TicketDetailsActivity, "Failed to resolve ticket", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@TicketDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
