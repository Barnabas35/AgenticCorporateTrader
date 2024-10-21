package com.example.act_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_menu)

        // Find the profile button and set its click listener
        val profileButton = findViewById<Button>(R.id.profileButton)
        profileButton.setOnClickListener {
            // Start ProfileActivity without animation
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }

        // Find the review button and set its click listener
        val reviewpageButton = findViewById<Button>(R.id.reviewpageButton)
        reviewpageButton.setOnClickListener {
            // Start ReviewPageActivity without animation
            val intent = Intent(this, ReviewPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }

        // Find the support button and set its click listener
        val supportButton = findViewById<Button>(R.id.supportButton)
        supportButton.setOnClickListener {
            // Start SupportPageActivity without animation
            val intent = Intent(this, SupportPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }
    }
}
