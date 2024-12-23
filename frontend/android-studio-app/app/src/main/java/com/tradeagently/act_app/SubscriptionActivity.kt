package com.tradeagently.act_app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var subscriptionStatusText: TextView
    private lateinit var subscriptionPeriodText: TextView
    private lateinit var subscriptionRenewText: TextView
    private lateinit var subscriptionPriceText: TextView
    private lateinit var activateSubscriptionButton: Button
    private lateinit var cancelSubscriptionButton: Button
    private lateinit var sessionToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        NavigationHelper.setupBottomNavigation(this, -1)

        subscriptionStatusText = findViewById(R.id.subscriptionStatusText)
        subscriptionPeriodText = findViewById(R.id.subscriptionPeriodText)
        subscriptionRenewText = findViewById(R.id.subscriptionRenewText)
        subscriptionPriceText = findViewById(R.id.subscriptionPriceText)
        activateSubscriptionButton = findViewById(R.id.activateSubscriptionButton)
        cancelSubscriptionButton = findViewById(R.id.cancelSubscriptionButton)

        sessionToken = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        getSubscriptionDetails()

        activateSubscriptionButton.setOnClickListener {
            activateSubscription()
        }

        cancelSubscriptionButton.setOnClickListener {
            cancelSubscription()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        activateSubscriptionButton.isEnabled = !isLoading
        cancelSubscriptionButton.isEnabled = !isLoading
    }

    private fun getSubscriptionDetails() {
        showLoading(true)
        val request = SubscriptionRequest(session_token = sessionToken)
        RetrofitClient.apiService.getSubscription(request).enqueue(object : Callback<SubscriptionResponse> {
            override fun onResponse(call: Call<SubscriptionResponse>, response: Response<SubscriptionResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val subscriptionDetails = response.body()
                    if (subscriptionDetails != null && subscriptionDetails.status == "success") {
                        val isActive = subscriptionDetails.subscription_active
                        val endDate = if (subscriptionDetails.subscription_end == 0L) {
                            "   -   "
                        } else {
                            DateFormat.getDateInstance().format(
                                Date(subscriptionDetails.subscription_end * 1000)
                            )
                        }
                        val autoRenew = if (subscriptionDetails.renew_subscription) "Yes" else "No"

                        subscriptionStatusText.text = "Subscription Status: ${if (isActive) "Active" else "Inactive"}"
                        subscriptionPeriodText.text = "Valid Until: $endDate"
                        subscriptionRenewText.text = "Auto-Renews: $autoRenew"

                        subscriptionStatusText.invalidate()
                        subscriptionPeriodText.invalidate()
                        subscriptionRenewText.invalidate()

                        activateSubscriptionButton.isEnabled = !isActive
                        cancelSubscriptionButton.isEnabled = isActive
                    } else if (subscriptionDetails?.status == "Insufficient balance.") {
                        Toast.makeText(
                            this@SubscriptionActivity,
                            "Insufficient balance to view subscription details.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@SubscriptionActivity, "Failed to fetch subscription details.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubscriptionResponse>, t: Throwable) {
                showLoading(false)
                Log.e("SubscriptionActivity", "Error: ${t.message}")
                Toast.makeText(this@SubscriptionActivity, "Error fetching subscription details.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun activateSubscription() {
        showLoading(true)
        val request = SubscriptionRequest(session_token = sessionToken)
        RetrofitClient.apiService.activateSubscription(request).enqueue(object : Callback<SubscriptionActionResponse> {
            override fun onResponse(call: Call<SubscriptionActionResponse>, response: Response<SubscriptionActionResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val actionResponse = response.body()
                    if (actionResponse?.status == "success") {
                        Toast.makeText(this@SubscriptionActivity, "Subscription activated successfully!", Toast.LENGTH_SHORT).show()
                        getSubscriptionDetails()
                    } else if (actionResponse?.status == "Insufficient balance.") {
                        Toast.makeText(this@SubscriptionActivity, "Insufficient balance to activate subscription.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SubscriptionActivity, "Failed to activate subscription.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubscriptionActionResponse>, t: Throwable) {
                showLoading(false)
                Log.e("SubscriptionActivity", "Error: ${t.message}")
                Toast.makeText(this@SubscriptionActivity, "Error activating subscription.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cancelSubscription() {
        showLoading(true)
        val request = SubscriptionRequest(session_token = sessionToken)
        RetrofitClient.apiService.cancelSubscription(request).enqueue(object : Callback<SubscriptionActionResponse> {
            override fun onResponse(call: Call<SubscriptionActionResponse>, response: Response<SubscriptionActionResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val actionResponse = response.body()
                    if (actionResponse?.status == "success") {
                        Toast.makeText(this@SubscriptionActivity, "Subscription canceled successfully.", Toast.LENGTH_SHORT).show()
                        getSubscriptionDetails()
                    } else {
                        Toast.makeText(this@SubscriptionActivity, "Failed to cancel subscription.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SubscriptionActivity, "Failed to cancel subscription.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SubscriptionActionResponse>, t: Throwable) {
                showLoading(false)
                Log.e("SubscriptionActivity", "Error: ${t.message}")
                Toast.makeText(this@SubscriptionActivity, "Error canceling subscription.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
