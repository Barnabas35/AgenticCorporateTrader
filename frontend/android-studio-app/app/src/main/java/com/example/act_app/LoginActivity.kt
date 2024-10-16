package com.example.act_app

import android.content.Intent
import android.os.Bundle
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

                        // Check for "status" == "Success"
                        if (apiResponse?.status == "Success") {
                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to MyAssetsActivity
                            val intent = Intent(this@LoginActivity, MyAssetsActivity::class.java)
                            startActivity(intent)
                            finish() // Optionally close the login activity
                        } else {
                            Toast.makeText(this@LoginActivity, apiResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
