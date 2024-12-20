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

    private lateinit var sharedPreferences: SharedPreferences
    private var sessionToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        NavigationHelper.setupBottomNavigation(this, -1)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", null)

        // Redirect to login if token is null
        if (sessionToken == null) {
            navigateToLogin()
            return
        }

        // Fetch and display user information each time the user opens this activity
        fetchUserDetails(sessionToken!!)

        // Logout button functionality
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            clearUserDetails()
            navigateToMainActivity()
        }

        // Delete account functionality with confirmation dialog
        // (We will set up the delete button after fetching user details)
    }

    private fun fetchUserDetails(token: String) {
        // 1) Fetch Username
        RetrofitClient.apiService.getUsername(TokenRequest(token)).enqueue(object : Callback<UsernameResponse> {
            override fun onResponse(call: Call<UsernameResponse>, response: Response<UsernameResponse>) {
                val username = if (response.isSuccessful && response.body()?.status == "Success") {
                    response.body()?.username ?: "Unknown"
                } else {
                    "Unknown"
                }

                // Save username
                sharedPreferences.edit().putString("user_name", username).apply()

                // Now fetch email
                fetchUserEmail(token)
            }

            override fun onFailure(call: Call<UsernameResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Failed to fetch username: ${t.message}")
                sharedPreferences.edit().putString("user_name", "Unknown").apply()
                // Still attempt to fetch email
                fetchUserEmail(token)
            }
        })
    }

    private fun fetchUserEmail(token: String) {
        RetrofitClient.apiService.getEmail(TokenRequest(token)).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                val email = if (response.isSuccessful && response.body()?.status == "Success") {
                    response.body()?.email ?: "Unknown"
                } else {
                    "Unknown"
                }

                // Save email
                sharedPreferences.edit().putString("user_email", email).apply()

                // Now fetch profile icon
                fetchProfileIcon(token)
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Failed to fetch email: ${t.message}")
                sharedPreferences.edit().putString("user_email", "Unknown").apply()
                // Still attempt to fetch profile icon
                fetchProfileIcon(token)
            }
        })
    }

    private fun fetchProfileIcon(token: String) {
        RetrofitClient.apiService.getProfileIcon(TokenRequest(token)).enqueue(object : Callback<ProfileIconResponse> {
            override fun onResponse(call: Call<ProfileIconResponse>, response: Response<ProfileIconResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val url = response.body()?.url
                    if (!url.isNullOrEmpty()) {
                        sharedPreferences.edit().putString("profile_icon_url", url).apply()
                    } else {
                        sharedPreferences.edit().remove("profile_icon_url").apply()
                    }
                } else {
                    sharedPreferences.edit().remove("profile_icon_url").apply()
                }

                // Fetch user type if needed, then set up UI
                fetchUserTypeIfNeeded(token)
            }

            override fun onFailure(call: Call<ProfileIconResponse>, t: Throwable) {
                Log.e("ProfileActivity", "Failed to fetch profile icon: ${t.message}")
                sharedPreferences.edit().remove("profile_icon_url").apply()
                // Fetch user type if needed, then set up UI
                fetchUserTypeIfNeeded(token)
            }
        })
    }

    private fun fetchUserTypeIfNeeded(token: String) {
        val currentUserType = sharedPreferences.getString("user_type", "Unknown") ?: "Unknown"
        if (currentUserType == "Unknown") {
            RetrofitClient.apiService.getUserType(TokenRequest(token)).enqueue(object : Callback<UserTypeResponse> {
                override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                    val userType = if (response.isSuccessful && response.body()?.status == "Success") {
                        response.body()?.user_type ?: "Unknown"
                    } else {
                        "Unknown"
                    }
                    sharedPreferences.edit().putString("user_type", userType).apply()

                    // Now that all details are fetched, setup UI
                    setupUI()
                }

                override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                    Log.e("ProfileActivity", "Failed to fetch user type: ${t.message}")
                    sharedPreferences.edit().putString("user_type", "Unknown").apply()
                    // Setup UI anyway
                    setupUI()
                }
            })
        } else {
            // If user type already known, just setup UI
            setupUI()
        }
    }

    private fun setupUI() {
        setupUserInfo(sharedPreferences)
        // Setup the delete account button now that we have user_type
        sessionToken?.let { setupDeleteAccountButton(sharedPreferences, it) }
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

        if (profileIconUrl != null) {
            Glide.with(this)
                .load(profileIconUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_error)
                // Apply the circle crop transformation
                .circleCrop()
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
        } else {
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
                    // Successful delete response
                    clearUserDetails()
                    navigateToLogin()
                } else if (response.code() == 500) {
                    // Assume deletion succeeded if server returns 500 but still deletes
                    Log.w("DELETE_ACCOUNT_WARNING", "Server returned 500, but account likely deleted.")
                    Toast.makeText(this@ProfileActivity, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
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
        sharedPreferences.edit().clear().apply()
    }
}
