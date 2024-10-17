package com.example.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the user is logged in (i.e., token exists)
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("api_token", null)

        if (token == null) {
            // If the token is null, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity
            return
        }

        // Set up click listener for the LOGIN button
        findViewById<Button>(R.id.button8).setOnClickListener {
            navigateTo(LoginActivity::class.java)
        }

        // Set up click listener for the REGISTER button
        findViewById<Button>(R.id.button6).setOnClickListener {
            navigateTo(RegisterActivity::class.java)
        }
    }

    // Helper function to start a new activity
    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
    }
}
