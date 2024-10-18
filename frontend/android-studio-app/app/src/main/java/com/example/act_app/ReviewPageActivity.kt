package com.example.act_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ReviewPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewpage)

        // Set up the bottom navigation and pass -1 since ReviewPageActivity has no associated nav item
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
            val reviewText = findViewById<EditText>(R.id.editTextReview).text.toString()
            val rating = findViewById<RatingBar>(R.id.ratingBar).rating
            val easeOfUseAnswer = findViewById<EditText>(R.id.editTextEaseOfUse).text.toString()
            val featuresAnswer = findViewById<EditText>(R.id.editTextFeatures).text.toString()
            val overallSatisfactionAnswer = findViewById<EditText>(R.id.editTextSatisfaction).text.toString()

            // Handle form submission - Additional Comments is optional
            if (easeOfUseAnswer.isNotEmpty() && featuresAnswer.isNotEmpty() && overallSatisfactionAnswer.isNotEmpty()) {
                Toast.makeText(this, "Thank you for your review!", Toast.LENGTH_LONG).show()

                // If rating is 4 or higher, open Play Store for user to rate the app
                if (rating >= 4) {
                    openPlayStoreForRating()
                }

                // Optionally, you can save the review or send it to a backend service here
            } else {
                Toast.makeText(this, "Please fill in all required fields before submitting.", Toast.LENGTH_SHORT).show()
            }
        }
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
