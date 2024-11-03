package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_menu)

        // Find the buttons
        val profileButton = findViewById<Button>(R.id.profileButton)
        val reviewpageButton = findViewById<Button>(R.id.reviewpageButton)
        val supportButton = findViewById<Button>(R.id.supportButton)
        val clientManagementButton = findViewById<Button>(R.id.clientManagementButton)

        // Retrieve the user type from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("user_type", "")

        // Conditionally show/hide buttons based on user type
        when (userType) {
            "admin", "fa" -> {
                // For "admin" and "fa", hide Client Management button
                clientManagementButton.visibility = View.GONE
            }
            "fm" -> {
                // For "fm", display all buttons (Profile, Review, Support, Client Management)
                clientManagementButton.visibility = View.VISIBLE
            }
            else -> {
                // Default case: hide Client Management button (handle unknown user types)
                clientManagementButton.visibility = View.GONE
            }
        }

        // Set click listeners for each button
        profileButton.setOnClickListener {
            // Start ProfileActivity without animation
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }

        reviewpageButton.setOnClickListener {
            // Start ReviewPageActivity without animation
            val intent = Intent(this, ReviewPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }

        supportButton.setOnClickListener {
            // Start SupportPageActivity without animation
            val intent = Intent(this, SupportPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }

        clientManagementButton.setOnClickListener {
            // Start ClientManagementActivity without animation
            val intent = Intent(this, ClientManagementActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)  // Disable the transition animation
        }
    }
}
