package com.example.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Find the bottom navigation view
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Explicitly deselect all menu items
        bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.menu.getItem(i).isChecked = false
        }

        // Retrieve token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        // If token is null, redirect to login screen
        if (token == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Find TextViews for username, email, user type, and profile image
        val usernameTextView = findViewById<TextView>(R.id.username)
        val emailTextView = findViewById<TextView>(R.id.email)
        val userTypeTextView = findViewById<TextView>(R.id.userType)  // New TextView for User Type
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        // Retrieve and display user's email and username from SharedPreferences
        val userEmail = sharedPreferences.getString("user_email", "Unknown") ?: "Unknown"
        val userName = sharedPreferences.getString("user_name", "Unknown") ?: "Unknown"
        val userType = sharedPreferences.getString("user_type", "Unknown") ?: "Unknown"  // Retrieve the user type

        usernameTextView.text = "Username: $userName"
        emailTextView.text = "Email: $userEmail"
        userTypeTextView.text = "User Type: ${getUserTypeDescription(userType)}"  // Display the user type

        // Retrieve and display profile icon URL
        val profileIconUrl = sharedPreferences.getString("profile_icon_url", null)
        if (profileIconUrl != null) {
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

        // Fetch user type if it's not already set in SharedPreferences
        if (userType == "Unknown") {
            fetchAndSaveUserType(token)
        }
    }

    // Function to fetch user type from the API
    private fun fetchAndSaveUserType(token: String) {
        RetrofitClient.apiService.getUserType(TokenRequest(token)).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val userType = response.body()?.user_type ?: "Unknown"
                    Log.d("USER_TYPE_FETCHED", "Fetched user type: $userType")
                    saveUserType(userType)
                    // Update the UI with the fetched user type
                    findViewById<TextView>(R.id.userType).text = "User Type: ${getUserTypeDescription(userType)}"
                } else {
                    Log.e("API_ERROR", "Failed to fetch user type: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching user type: ${t.message}")
            }
        })
    }

    // Function to save user type to SharedPreferences
    private fun saveUserType(userType: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_type", userType)
        editor.apply()
    }

    // Function to convert user type codes to full descriptions
    private fun getUserTypeDescription(userType: String): String {
        return when (userType) {
            "fa" -> "Fund Administrator"
            "fm" -> "Fund Manager"
            "admin" -> "Admin"
            else -> "Unknown"
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure the navigation view doesn't select any item for ProfileActivity
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Explicitly deselect all items again to ensure nothing is selected
        bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.menu.getItem(i).isChecked = false
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
