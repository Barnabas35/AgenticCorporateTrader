package com.tradeagently.act_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TicketDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticketdetail)

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy)

        // Find TextViews in the layout
        val subjectTextView: TextView = findViewById(R.id.subjectTextView)
        val userIdTextView: TextView = findViewById(R.id.userIdTextView)
        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
        val statusTextView: TextView = findViewById(R.id.statusTextView)
        val ticketIdTextView: TextView = findViewById(R.id.ticketIdTextView)
        val timestampTextView: TextView = findViewById(R.id.timestampTextView)

        // Get data from intent extras
        val issueSubject = intent.getStringExtra("issue_subject")
        val userId = intent.getStringExtra("user_id")
        val issueDescription = intent.getStringExtra("issue_description")
        val issueStatus = intent.getStringExtra("issue_status")
        val ticketId = intent.getStringExtra("ticket_id")
        val unixTimestamp = intent.getLongExtra("unix_timestamp", 0)

        // Convert timestamp to a readable date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(unixTimestamp * 1000)) // Convert seconds to milliseconds

        // Set data to TextViews
        subjectTextView.text = issueSubject ?: "N/A"
        userIdTextView.text = "User ID: ${userId ?: "N/A"}"
        descriptionTextView.text = "Description: ${issueDescription ?: "N/A"}"
        statusTextView.text = "Status: ${issueStatus ?: "N/A"}"
        ticketIdTextView.text = "Ticket ID: ${ticketId ?: "N/A"}"
        timestampTextView.text = "Timestamp: $formattedDate"
    }
}
