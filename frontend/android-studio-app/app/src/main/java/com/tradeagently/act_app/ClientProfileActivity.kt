package com.tradeagently.act_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClientProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_profile)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, -1)


        // Get client name passed via Intent
        val clientName = intent.getStringExtra("CLIENT_NAME") ?: "Unknown Client"

        // Display client name
        findViewById<TextView>(R.id.clientNameTextView).text = clientName
    }
}
