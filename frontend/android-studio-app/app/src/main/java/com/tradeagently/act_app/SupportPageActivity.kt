package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SupportPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supportpage)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Get references to the EditTexts, Button, and phone number TextView
        val issueSubjectEditText: EditText = findViewById(R.id.editTextIssueSubject)
        val issueDescriptionEditText: EditText =
            findViewById(R.id.editTextReview)  // Updated reference
        val submitButton: Button = findViewById(R.id.buttonSubmitSupportTicket)
        val phoneNumberTextView: TextView = findViewById(R.id.textViewPhoneNumber)

        // Retrieve session token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        // Set up the submit button click listener
        submitButton.setOnClickListener {
            val subject = issueSubjectEditText.text.toString().trim()
            val description = issueDescriptionEditText.text.toString().trim()

            // Validate input
            if (subject.isEmpty()) {
                issueSubjectEditText.error = "Please enter a subject"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                issueDescriptionEditText.error = "Please describe the issue"
                return@setOnClickListener
            }

            if (token != null) {
                // Create the support ticket request
                val supportTicketRequest = SupportTicketRequest(
                    session_token = token,
                    issue_subject = subject,
                    issue_description = description
                )

                // Make the API call to submit the support ticket
                RetrofitClient.apiService.submitSupportTicket(supportTicketRequest)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: Response<ApiResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.status == "Success") {
                                Toast.makeText(
                                    this@SupportPageActivity,
                                    "Support ticket submitted!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Clear input fields after successful submission
                                issueSubjectEditText.text.clear()
                                issueDescriptionEditText.text.clear()
                            } else {
                                Toast.makeText(
                                    this@SupportPageActivity,
                                    "Failed to submit ticket",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(
                                this@SupportPageActivity,
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Set up the phone number click listener
        phoneNumberTextView.setOnClickListener {
            val phoneNumber = "08001818181"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }
}