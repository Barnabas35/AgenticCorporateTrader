package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AlertDialog
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
        val userTypeTextView = findViewById<TextView>(R.id.userType)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        // Retrieve and display user's email and username from SharedPreferences
        val userEmail = sharedPreferences.getString("user_email", "Unknown") ?: "Unknown"
        val userName = sharedPreferences.getString("user_name", "Unknown") ?: "Unknown"
        val userType = sharedPreferences.getString("user_type", "Unknown") ?: "Unknown"

        usernameTextView.text = "Username: $userName"
        emailTextView.text = "Email: $userEmail"
        userTypeTextView.text = "User Type: ${getUserTypeDescription(userType)}"

        // Retrieve and display profile icon URL
        val profileIconUrl = sharedPreferences.getString("profile_icon_url", null)
        if (profileIconUrl != null) {
            Glide.with(this)
                .load(profileIconUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_error)
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

        // Find the Delete Account button and set visibility based on user type
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        if (userType == "admin") {
            deleteAccountButton.visibility = View.GONE // Hide for admin
        } else {
            deleteAccountButton.visibility = View.VISIBLE // Show for "fa" and "fm"
            deleteAccountButton.setOnClickListener {
                // Show a confirmation dialog
                AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteAccount(token)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        // Fetch user type if it's not already set in SharedPreferences
        if (userType == "Unknown") {
            fetchAndSaveUserType(token)
        }
    }

    private fun deleteAccount(token: String) {
        val requestBody = mapOf("session_token" to token)

        RetrofitClient.apiService.deleteUser(requestBody).enqueue(object : Callback<DeleteUserResponse> {
            override fun onResponse(call: Call<DeleteUserResponse>, response: Response<DeleteUserResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    clearUserDetails()
                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("DELETE_ACCOUNT_ERROR", "Failed to delete account: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ProfileActivity, "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteUserResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error deleting account: ${t.message}")
                Toast.makeText(this@ProfileActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAndSaveUserType(token: String) {
        RetrofitClient.apiService.getUserType(TokenRequest(token)).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val userType = response.body()?.user_type ?: "Unknown"
                    Log.d("USER_TYPE_FETCHED", "Fetched user type: $userType")
                    saveUserType(userType)
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

    private fun saveUserType(userType: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("user_type", userType).apply()
    }

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
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.menu.getItem(i).isChecked = false
        }
    }

    private fun clearUserDetails() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
