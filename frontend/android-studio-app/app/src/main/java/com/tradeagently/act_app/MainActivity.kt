package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        if (isUserLoggedIn()) {
            // User is logged in, navigate directly to MyAssetsActivity
            navigateToMyAssets()
            finish() // Close MainActivity so the user does not return to it
        } else {
            // User is not logged in, show the main layout with login and register options
            setContentView(R.layout.activity_main)

            // Set up the Login button to navigate to LoginActivity
            findViewById<Button>(R.id.loginButton).setOnClickListener {
                navigateToLogin()
            }

            // Set up the Register button to navigate to RegisterActivity
            findViewById<Button>(R.id.registerButton).setOnClickListener {
                navigateToRegister()
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        // Retrieve the session token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)
        return sessionToken != null
    }

    private fun navigateToLogin() {
        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToRegister() {
        // Navigate to RegisterActivity
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMyAssets() {
        // Navigate to MyAssetsActivity
        val intent = Intent(this, MyAssetsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
