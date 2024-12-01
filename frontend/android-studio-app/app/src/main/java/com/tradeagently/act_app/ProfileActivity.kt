package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        NavigationHelper.setupBottomNavigation(this, -1)

        // Retrieve token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        // Redirect to login if token is null
        if (token == null) {
            navigateToLogin()
            return
        }

        // Retrieve and display user information
        setupUserInfo(sharedPreferences)

        // Logout button functionality
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            clearUserDetails()
            navigateToMainActivity()
        }

        // Delete account functionality with confirmation dialog
        setupDeleteAccountButton(sharedPreferences, token)

        // Fetch and save user type if not set
        if (sharedPreferences.getString("user_type", "Unknown") == "Unknown") {
            fetchAndSaveUserType(token)
        }
    }

    private fun setupUserInfo(sharedPreferences: SharedPreferences) {
        val usernameTextView = findViewById<TextView>(R.id.username)
        val emailTextView = findViewById<TextView>(R.id.email)
        val userTypeTextView = findViewById<TextView>(R.id.userType)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        val userEmail = sharedPreferences.getString("user_email", "Unknown") ?: "Unknown"
        val userName = sharedPreferences.getString("user_name", "Unknown") ?: "Unknown"
        val userType = sharedPreferences.getString("user_type", "Unknown") ?: "Unknown"

        usernameTextView.text = "Username: $userName"
        emailTextView.text = "Email: $userEmail"
        userTypeTextView.text = "User Type: ${getUserTypeDescription(userType)}"

        val profileIconUrl = sharedPreferences.getString("profile_icon_url", null)

        // Load the profile image using Glide with enhanced error handling
        profileIconUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_error)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        e?.logRootCauses("GlideError")
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed to load profile image",
                            Toast.LENGTH_SHORT
                        ).show()
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(profileImageView)
        } ?: run {
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun setupDeleteAccountButton(sharedPreferences: SharedPreferences, token: String) {
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        val userType = sharedPreferences.getString("user_type", "Unknown")

        deleteAccountButton.visibility = if (userType == "admin") View.GONE else View.VISIBLE
        deleteAccountButton.setOnClickListener {
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

    private fun deleteAccount(token: String) {
        val requestBody = mapOf("session_token" to token)

        RetrofitClient.apiService.deleteUser(requestBody).enqueue(object : Callback<DeleteUserResponse> {
            override fun onResponse(call: Call<DeleteUserResponse>, response: Response<DeleteUserResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    clearUserDetails()
                    navigateToLogin()
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

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun clearUserDetails() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
