package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeFirebase()

        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val registerTextView: TextView = findViewById(R.id.textViewRegister)
        val googleSignInButton: SignInButton = findViewById(R.id.buttonGoogleSignIn)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("68009005920-gkrtmda8m8hrq0273t5ptgsr1voivf0n.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }

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

            performLogin(email, password)
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
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
                                        exchangeTokens(firebaseToken)
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

    private fun exchangeTokens(authToken: String) {
        val request = ExchangeTokensRequest(auth_token = authToken)

        RetrofitClient.apiService.exchangeTokens(request).enqueue(object : Callback<ExchangeTokensResponse> {
            override fun onResponse(
                call: Call<ExchangeTokensResponse>,
                response: Response<ExchangeTokensResponse>
            ) {
                if (response.isSuccessful) {
                    val exchangeResponse = response.body()
                    when (exchangeResponse?.status) {
                        "Success" -> {
                            exchangeResponse.session_token?.let {
                                saveSessionToken(it)
                            }
                            navigateToAssetsActivity()
                        }
                        "Success: Register User" -> {
                            showUserTypeSelectionDialog(authToken)
                        }
                        "Invalid auth token." -> {
                            Toast.makeText(this@LoginActivity, "Invalid token. Please try signing in again.", Toast.LENGTH_LONG).show()
                            googleSignInClient.signOut().addOnCompleteListener {
                                val signInIntent = googleSignInClient.signInIntent
                                startActivityForResult(signInIntent, RC_SIGN_IN)
                            }
                        }
                        else -> {
                            Toast.makeText(this@LoginActivity, "Unexpected status: ${exchangeResponse?.status}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Error exchanging tokens.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ExchangeTokensResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveSessionToken(sessionToken: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("session_token", sessionToken).apply()
    }

    private fun showUserTypeSelectionDialog(firebaseToken: String) {
        val userTypeOptions = arrayOf("Fund Administrator", "Fund Manager")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select User Type")
        builder.setItems(userTypeOptions) { dialog, which ->
            val selectedUserType = when (userTypeOptions[which]) {
                "Fund Administrator" -> "fa"
                "Fund Manager" -> "fm"
                else -> throw IllegalArgumentException("Unknown user type selected.")
            }

            dialog.dismiss()
            registerWithToken(firebaseToken, selectedUserType)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun registerWithToken(authToken: String, userType: String) {
        val request = RegisterWithTokenRequest(auth_token = authToken, user_type = userType)

        RetrofitClient.apiService.registerWithToken(request).enqueue(object : Callback<RegisterWithTokenResponse> {
            override fun onResponse(call: Call<RegisterWithTokenResponse>, response: Response<RegisterWithTokenResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@LoginActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Failed to register with token.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterWithTokenResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun performLogin(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.status == "Success" && apiResponse.session_token != null) {
                        saveUserDetails(apiResponse.session_token)
                        navigateToAssetsActivity()
                    } else {
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

    private fun saveUserDetails(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("session_token", token).apply()
    }

    private fun navigateToAssetsActivity() {
        startActivity(Intent(this@LoginActivity, MyAssetsActivity::class.java))
        finish()
    }

    private fun initializeFirebase() {
        val options = FirebaseOptions.Builder()
            .setApiKey("AIzaSyDRxJ8lASX6gQrHbmK8hcmUjplWy-aLuko")
            .setApplicationId("1:68009005920:android:2c5b77919be0a2d0e60405")
            .setProjectId("agenticcorporatetrader")
            .setDatabaseUrl("https://agenticcorporatetrader-default-rtdb.europe-west1.firebasedatabase.app")
            .build()

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Error initializing Firebase: ${e.message}")
        }
    }
}
