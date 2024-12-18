package com.tradeagently.act_app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001 // Request code for Google Sign-In
    private var userTypeValue: String = ""

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
        val googleSignInButton: com.google.android.gms.common.SignInButton = findViewById(R.id.buttonGoogleSignIn)

        // Configure user type spinner
        val userTypeOptions = arrayOf("User Type", "Fund Administrator", "Fund Manager")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter
        userTypeSpinner.setSelection(0)

        // Handle normal registration via email/password
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val selectedUserType = userTypeSpinner.selectedItem.toString()

            // Validate fields
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

            if (selectedUserType == "User Type") {
                Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Map user-friendly names to API-expected values
            userTypeValue = when (selectedUserType) {
                "Fund Administrator" -> "fa"
                "Fund Manager" -> "fm"
                else -> ""
            }

            // Create registration request
            val registerRequest = RegisterRequest(username, email, password, userTypeValue)
            registerViaEmailPassword(registerRequest)
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Replace with your web client ID
            .requestIdToken("YOUR-WEB-CLIENT-ID.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
            // Clear cached token before initiating sign-in
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    handleGoogleSignIn(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        val googleIdToken = account.idToken
        if (googleIdToken != null) {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            user.getIdToken(true).addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val firebaseToken = tokenTask.result?.token
                                    if (!firebaseToken.isNullOrEmpty()) {
                                        // Register user with token
                                        // Assign a default user type if not chosen yet (e.g., "fa" or "fm")
                                        // Or show a prompt to the user to pick a type before calling this
                                        val defaultUserType = "fa" // Or you can prompt user to select
                                        registerWithToken(firebaseToken, defaultUserType)
                                    } else {
                                        Toast.makeText(this, "Could not retrieve Firebase token", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to get Firebase token: ${tokenTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "No Firebase user available after sign-in", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Firebase sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "Failed to retrieve Google ID token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerViaEmailPassword(registerRequest: RegisterRequest) {
        RetrofitClient.apiService.register(registerRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "Success") {
                        Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
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

    private fun registerWithToken(authToken: String, userType: String) {
        val request = RegisterWithTokenRequest(auth_token = authToken, user_type = userType)
        RetrofitClient.apiService.registerWithToken(request).enqueue(object : Callback<RegisterWithTokenResponse> {
            override fun onResponse(
                call: Call<RegisterWithTokenResponse>,
                response: Response<RegisterWithTokenResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@RegisterActivity, "Registration successful via Google!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Failed to register with token.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterWithTokenResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}