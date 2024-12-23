package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        NavigationHelper.setupBottomNavigation(this, -1)

        val issueSubjectEditText: EditText = findViewById(R.id.editTextIssueSubject)
        val issueDescriptionEditText: EditText =
            findViewById(R.id.editTextReview)  // Updated reference
        val submitButton: Button = findViewById(R.id.buttonSubmitSupportTicket)
        val phoneNumberTextView: TextView = findViewById(R.id.textViewPhoneNumber)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        submitButton.setOnClickListener {
            val subject = issueSubjectEditText.text.toString().trim()
            val description = issueDescriptionEditText.text.toString().trim()

            if (subject.isEmpty()) {
                issueSubjectEditText.error = "Please enter a subject"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                issueDescriptionEditText.error = "Please describe the issue"
                return@setOnClickListener
            }

            if (token != null) {
                val supportTicketRequest = SupportTicketRequest(
                    session_token = token,
                    issue_subject = subject,
                    issue_description = description
                )

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

        phoneNumberTextView.setOnClickListener {
            val phoneNumber = "08001818181"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }
}