package com.tradeagently.act_app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText: EditText = findViewById(R.id.editTextUsername)
        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val confirmPasswordEditText: EditText = findViewById(R.id.editTextConfirmPassword)
        val userTypeSpinner: Spinner = findViewById(R.id.spinnerUserType)
        val registerButton: Button = findViewById(R.id.buttonRegister)
        val loginTextView: TextView = findViewById(R.id.textViewLogin)

        // Spinner options including a default "User Type" at the start
        val userTypeOptions = arrayOf("User Type", "Fund Administrator", "Fund Manager")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter

        // Ensure "User Type" is not selectable
        userTypeSpinner.setSelection(0)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val selectedUserType = userTypeSpinner.selectedItem.toString()

            // Validate the input fields
            if (username.isEmpty()) {
                usernameEditText.error = "Please enter your username"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter your password"
                return@setOnClickListener
            }

            if (confirmPassword != password) {
                confirmPasswordEditText.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Ensure user type is selected
            if (selectedUserType == "User Type") {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Map user-friendly names to API-expected values
            val userTypeValue = when (selectedUserType) {
                "Fund Administrator" -> "fa"
                "Fund Manager" -> "fm"
                else -> ""
            }

            // Create registration request including the selected user type
            val registerRequest = RegisterRequest(username, email, password, userTypeValue)

            // Make API call for registration
            RetrofitClient.apiService.register(registerRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "Success") {
                            Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()

                            // Navigate to LoginActivity after successful registration
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, apiResponse?.status ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Navigate to login screen when the login link is clicked
        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
