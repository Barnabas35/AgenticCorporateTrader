package com.example.act_app

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

            // Make API call
            RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()

                        // Log the raw response for debugging purposes
                        Log.d("API_RESPONSE", "Raw response: ${response.body()}")

                        // Check if status is "Success" or if success flag is true
                        if (apiResponse?.status == "Success" || apiResponse?.success == true) {
                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to MyAssetsActivity
                            val intent = Intent(this@LoginActivity, MyAssetsActivity::class.java)
                            startActivity(intent)
                            finish() // Optionally close the login activity
                        } else {
                            // Log the API response message
                            Log.d("API_RESPONSE_ERROR", "API Message: ${apiResponse?.message}")
                            Toast.makeText(this@LoginActivity, apiResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Handle HTTP error codes (non-2xx)
                        Toast.makeText(this@LoginActivity, "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // Handle network or other failures
                    Log.e("NETWORK_ERROR", "Failure: ${t.message}")
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Register link click listener
        registerTextView.setOnClickListener {
            // Navigate to RegisterActivity when the link is clicked
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
