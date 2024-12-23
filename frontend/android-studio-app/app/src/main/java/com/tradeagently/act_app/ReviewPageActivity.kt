package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        NavigationHelper.setupBottomNavigation(this, -1)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User")

        val nameEditText = findViewById<EditText>(R.id.editTextName)
        nameEditText.setText(userName)

        val submitButton = findViewById<Button>(R.id.buttonSubmit)
        submitButton.setOnClickListener {
            val reviewText = findViewById<EditText>(R.id.editTextReview).text.toString()
            val rating = findViewById<RatingBar>(R.id.ratingBar).rating.toInt()
            val easeOfUseAnswer = findViewById<EditText>(R.id.editTextEaseOfUse).text.toString()
            val featuresAnswer = findViewById<EditText>(R.id.editTextFeatures).text.toString()
            val overallSatisfactionAnswer = findViewById<EditText>(R.id.editTextSatisfaction).text.toString()

            val reviewComment = "Review: $reviewText\nEase of Use: $easeOfUseAnswer\nFeatures: $featuresAnswer\nOverall Satisfaction: $overallSatisfactionAnswer"

            if (easeOfUseAnswer.isNotEmpty() && featuresAnswer.isNotEmpty() && overallSatisfactionAnswer.isNotEmpty()) {
                val sessionToken = sharedPreferences.getString("session_token", null)
                Log.d("SessionToken", "Retrieved token: $sessionToken")

                if (sessionToken != null) {
                    submitReview(sessionToken, rating, reviewComment)
                } else {
                    Toast.makeText(this, "Session token is missing. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all required fields before submitting.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitReview(sessionToken: String, rating: Int, reviewComment: String) {
        val reviewRequest = ReviewRequest(
            session_token = sessionToken,
            review_score = rating,
            review_comment = reviewComment
        )

        RetrofitClient.apiService.submitReview(reviewRequest).enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                if (response.isSuccessful) {
                    val reviewResponse = response.body()
                    if (reviewResponse?.status == "Success") {
                        Toast.makeText(this@ReviewPageActivity, "Review submitted successfully!", Toast.LENGTH_LONG).show()
                        if (rating >= 4) {
                            openPlayStoreForRating()
                        }
                    } else {
                        Toast.makeText(this@ReviewPageActivity, "Error: ${reviewResponse?.status}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ReviewPageActivity, "Failed to submit review: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                Toast.makeText(this@ReviewPageActivity, "Error submitting review: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun openPlayStoreForRating() {
        val packageName = applicationContext.packageName
        val playStoreUri = Uri.parse("market://details?id=$packageName")
        val playStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri)

        if (playStoreIntent.resolveActivity(packageManager) != null) {
            startActivity(playStoreIntent)
        } else {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_dummy
    }
}
