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
        val adminToolsButton = findViewById<Button>(R.id.adminToolsButton)
        val priceAlertsButton = findViewById<Button>(R.id.priceAlertsButton)

        // Retrieve the user type from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userType = sharedPreferences.getString("user_type", "")

        // Conditionally show/hide buttons based on user type
        when (userType) {
            "admin" -> {
                adminToolsButton.visibility = View.VISIBLE
                clientManagementButton.visibility = View.GONE
            }
            "fa" -> {
                clientManagementButton.visibility = View.GONE
                adminToolsButton.visibility = View.GONE
            }
            "fm" -> {
                clientManagementButton.visibility = View.VISIBLE
                adminToolsButton.visibility = View.GONE
            }
            else -> {
                adminToolsButton.visibility = View.GONE
                clientManagementButton.visibility = View.GONE
            }
        }

        // Set click listeners for each button
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        reviewpageButton.setOnClickListener {
            val intent = Intent(this, ReviewPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        supportButton.setOnClickListener {
            val intent = Intent(this, SupportPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        clientManagementButton.setOnClickListener {
            val intent = Intent(this, ClientManagementActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        adminToolsButton.setOnClickListener {
            val intent = Intent(this, AdminToolsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        priceAlertsButton.setOnClickListener {
            val intent = Intent(this, PriceAlertsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }
}