package com.example.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerTextView: TextView = findViewById(R.id.textViewRegister)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter your password"
                return@setOnClickListener
            }

            // Create login request
            val loginRequest = LoginRequest(email, password)

            // Make API call for login
            RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()

                        // Log the raw response for debugging purposes
                        Log.d("API_RESPONSE", "Raw response: ${response.body()}")

                        if (apiResponse?.status == "Success" && apiResponse.session_token != null) {
                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                            // Save token to SharedPreferences
                            val token = apiResponse.session_token
                            saveUserDetails(token)

                            // Fetch username, email, and profile icon
                            fetchAndSaveUsername(token)
                            fetchAndSaveEmail(token)
                            fetchAndSaveProfileIcon(token)

                            // Navigate to MyAssetsActivity
                            val intent = Intent(this@LoginActivity, MyAssetsActivity::class.java)
                            startActivity(intent)
                            finish() // Optionally close the login activity
                        } else {
                            Log.d("API_RESPONSE_ERROR", "API Message: ${apiResponse?.status}")
                            Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("NETWORK_ERROR", "Failure: ${t.message}")
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Register link click listener
        registerTextView.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Save token and other details to SharedPreferences
    private fun saveUserDetails(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("api_token", token)
        editor.apply()
    }

    // Fetch and save username
    private fun fetchAndSaveUsername(token: String) {
        RetrofitClient.apiService.getUsername(TokenRequest(token)).enqueue(object : Callback<UsernameResponse> {
            override fun onResponse(call: Call<UsernameResponse>, response: Response<UsernameResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val username = response.body()?.username ?: "Unknown"
                    Log.d("USERNAME_FETCHED", "Fetched username: $username")
                    saveUsername(username)
                } else {
                    Log.e("API_ERROR", "Failed to fetch username: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UsernameResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching username: ${t.message}")
            }
        })
    }

    // Save username to SharedPreferences
    private fun saveUsername(username: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_name", username)
        editor.apply()
    }

    // Fetch and save email
    private fun fetchAndSaveEmail(token: String) {
        RetrofitClient.apiService.getEmail(TokenRequest(token)).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val email = response.body()?.email ?: "Unknown"
                    Log.d("EMAIL_FETCHED", "Fetched email: $email")
                    saveEmail(email)
                } else {
                    Log.e("API_ERROR", "Failed to fetch email: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching email: ${t.message}")
            }
        })
    }

    // Save email to SharedPreferences
    private fun saveEmail(email: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_email", email)
        editor.apply()
    }

    // Fetch and save profile icon
    private fun fetchAndSaveProfileIcon(token: String) {
        RetrofitClient.apiService.getProfileIcon(TokenRequest(token)).enqueue(object : Callback<ProfileIconResponse> {
            override fun onResponse(call: Call<ProfileIconResponse>, response: Response<ProfileIconResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val profileIconUrl = response.body()?.url ?: "Unknown"
                    Log.d("PROFILE_ICON_FETCHED", "Fetched profile icon URL: $profileIconUrl")
                    saveProfileIconUrl(profileIconUrl)
                } else {
                    Log.e("API_ERROR", "Failed to fetch profile icon URL: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ProfileIconResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Error fetching profile icon: ${t.message}")
            }
        })
    }

    // Save profile icon URL to SharedPreferences
    private fun saveProfileIconUrl(profileIconUrl: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profile_icon_url", profileIconUrl)
        editor.apply()
    }
}
