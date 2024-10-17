package com.example.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set up the bottom navigation, pass -1 since ProfileActivity has no associated nav item
        NavigationHelper.setupBottomNavigation(this, -1)

        // Retrieve token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("api_token", null)

        // If token is null, redirect to login screen
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Find TextViews for username and email
        val usernameTextView = findViewById<TextView>(R.id.username)
        val emailTextView = findViewById<TextView>(R.id.email)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        // Retrieve and display user's email and username from SharedPreferences
        val userEmail = sharedPreferences.getString("user_email", "Unknown")
        val userName = sharedPreferences.getString("user_name", "Unknown")
        usernameTextView.text = "Username: $userName"
        emailTextView.text = "Email: $userEmail"

        // Retrieve and display profile icon URL
        val profileIconUrl = sharedPreferences.getString("profile_icon_url", null)
        if (profileIconUrl != null) {
            // Use Glide to load the profile icon URL into the ImageView
            Glide.with(this)
                .load(profileIconUrl)
                .placeholder(R.drawable.ic_profile_placeholder) // Optional placeholder image
                .error(R.drawable.ic_error) // Optional error image
                .into(profileImageView)
        }

        // Find the Logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            clearUserDetails()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Function to clear the token and user details in SharedPreferences
    private fun clearUserDetails() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Clear all saved user details
        editor.apply()
    }
}
