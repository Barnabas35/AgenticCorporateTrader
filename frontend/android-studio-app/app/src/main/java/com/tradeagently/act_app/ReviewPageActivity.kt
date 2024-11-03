package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewpage)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Retrieve user's name from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")

        // Set user's name in the name input field
        val nameEditText = findViewById<EditText>(R.id.editTextName)
        nameEditText.setText(userName)

        // Find the Submit button and set its click listener
        val submitButton = findViewById<Button>(R.id.buttonSubmit)
        submitButton.setOnClickListener {
            // Gather input data
            val reviewText = findViewById<EditText>(R.id.editTextReview).text.toString()
            val rating = findViewById<RatingBar>(R.id.ratingBar).rating.toInt()
            val easeOfUseAnswer = findViewById<EditText>(R.id.editTextEaseOfUse).text.toString()
            val featuresAnswer = findViewById<EditText>(R.id.editTextFeatures).text.toString()
            val overallSatisfactionAnswer = findViewById<EditText>(R.id.editTextSatisfaction).text.toString()

            // Check that all required fields are filled
            if (easeOfUseAnswer.isNotEmpty() && featuresAnswer.isNotEmpty() && overallSatisfactionAnswer.isNotEmpty()) {
                // Retrieve session token from SharedPreferences
                val sessionToken = sharedPreferences.getString("session_token", null)
                Log.d("SessionToken", "Retrieved token: $sessionToken") // Log for debugging

                // Ensure session token is available
                if (sessionToken != null) {
                    // Submit the review
                    submitReview(sessionToken, rating, reviewText)
                } else {
                    Toast.makeText(this, "Session token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all required fields before submitting.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to submit the review to the API using RetrofitClient
    private fun submitReview(sessionToken: String, rating: Int, reviewComment: String) {
        val reviewRequest = ReviewRequest(
            session_token = sessionToken,
            review_score = rating,
            review_comment = reviewComment
        )

        // Use RetrofitClient to submit the review
        RetrofitClient.apiService.submitReview(reviewRequest).enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    if (reviewResponse?.status == "Success") {
                        // Show success message
                        Toast.makeText(this@ReviewPageActivity, "Review submitted successfully!", Toast.LENGTH_LONG).show()

                        // If rating is 4 or higher, open Play Store for user to rate the app
                        if (rating >= 4) {
                            openPlayStoreForRating()
                        }
                    } else {
                        // Handle API error message
                        Toast.makeText(this@ReviewPageActivity, "Error: ${reviewResponse?.status}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle unsuccessful response
                    Toast.makeText(this@ReviewPageActivity, "Failed to submit review: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                // Handle failure to connect to the API
                Toast.makeText(this@ReviewPageActivity, "Error submitting review: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openPlayStoreForRating() {
        val packageName = applicationContext.packageName
        val playStoreUri = Uri.parse("market://details?id=$packageName")
        val playStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri)

        // If Google Play Store is available, open the store, otherwise open in a browser
        if (playStoreIntent.resolveActivity(packageManager) != null) {
            startActivity(playStoreIntent)
        } else {
            // Fallback to browser if Play Store is not available
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure the navigation view doesn't select any item for ReviewPageActivity
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Explicitly select the hidden dummy item to ensure nothing else appears selected
        bottomNavigationView.selectedItemId = R.id.nav_dummy
    }
}
