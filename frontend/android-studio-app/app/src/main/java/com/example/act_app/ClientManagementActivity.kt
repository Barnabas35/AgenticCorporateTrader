package com.example.act_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ClientManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_management)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Here you can initialize any buttons, views, or add functionality for the Client Management Activity
    }
}
