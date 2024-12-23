package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuActivity : AppCompatActivity() {

    private lateinit var profileButton: Button
    private lateinit var aiSubscriptionButton: Button
    private lateinit var priceAlertsButton: Button
    private lateinit var reviewpageButton: Button
    private lateinit var supportButton: Button
    private lateinit var clientManagementButton: Button
    private lateinit var adminToolsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        NavigationHelper.setupBottomNavigation(this, R.id.nav_menu)

        profileButton = findViewById(R.id.profileButton)
        aiSubscriptionButton = findViewById(R.id.aiSubscriptionButton)
        reviewpageButton = findViewById(R.id.reviewpageButton)
        supportButton = findViewById(R.id.supportButton)
        clientManagementButton = findViewById(R.id.clientManagementButton)
        adminToolsButton = findViewById(R.id.adminToolsButton)
        priceAlertsButton = findViewById(R.id.priceAlertsButton)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val storedUserType = sharedPreferences.getString("user_type", null)

        if (storedUserType.isNullOrEmpty()) {
            fetchUserTypeFromServer()
        } else {
            setUIBasedOnUserType(storedUserType)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        aiSubscriptionButton.setOnClickListener {
            val intent = Intent(this, SubscriptionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        reviewpageButton.setOnClickListener {
            val intent = Intent(this, ReviewPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        supportButton.setOnClickListener {
            val intent = Intent(this, SupportPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        clientManagementButton.setOnClickListener {
            val intent = Intent(this, ClientManagementActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        adminToolsButton.setOnClickListener {
            val intent = Intent(this, AdminToolsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        priceAlertsButton.setOnClickListener {
            val intent = Intent(this, PriceAlertsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun fetchUserTypeFromServer() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPreferences.getString("session_token", null)

        if (sessionToken.isNullOrEmpty()) {
            Toast.makeText(this, "No session token found. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getUserType(request).enqueue(object : Callback<UserTypeResponse> {
            override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val userType = response.body()?.user_type
                    if (!userType.isNullOrEmpty()) {
                        val editor = sharedPreferences.edit()
                        editor.putString("user_type", userType)
                        editor.apply()

                        setUIBasedOnUserType(userType)
                    } else {
                        Toast.makeText(this@MenuActivity, "Failed to retrieve user type.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MenuActivity, "Failed to fetch user type from server.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                Toast.makeText(this@MenuActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUIBasedOnUserType(userType: String) {
        when (userType) {
            "admin" -> {
                adminToolsButton.visibility = View.VISIBLE
                clientManagementButton.visibility = View.GONE
            }
            "fa" -> {
                clientManagementButton.visibility = View.GONE
                adminToolsButton.visibility = View.GONE
            }
            "fm" -> {
                clientManagementButton.visibility = View.VISIBLE
                adminToolsButton.visibility = View.GONE
            }
            else -> {
                adminToolsButton.visibility = View.GONE
                clientManagementButton.visibility = View.GONE
            }
        }
    }
}
