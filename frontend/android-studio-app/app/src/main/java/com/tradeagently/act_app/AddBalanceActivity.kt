package com.tradeagently.act_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.tradeagently.act_app.R.layout.activity_add_balance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddBalanceActivity : AppCompatActivity() {

    private lateinit var sessionToken: String
    private lateinit var balanceInput: EditText
    private lateinit var submitButton: Button
    private lateinit var paymentSheet: PaymentSheet
    private var clientSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_add_balance)

        // Initialize Stripe PaymentConfiguration
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51QP2IwFp664itGdOwg1hyEpDcLxfaD29psic6hcZ5lnmO6MUZNXnu0Vft1kZk8pLx4BGc6ofKD9oZS4pHPdBj5tz00lLw5IBU5" // Replace with your Stripe publishable key
        )

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // Get session token from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sessionToken = sharedPreferences.getString("session_token", "") ?: ""

        if (sessionToken.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy)

        balanceInput = findViewById(R.id.balanceInput)
        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            val amount = balanceInput.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                addBalance(amount)
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addBalance(amount: Int) {
        val request = AddBalanceRequest(sessionToken, amount)
        RetrofitClient.apiService.addBalance(request).enqueue(object : Callback<AddBalanceResponse> {
            override fun onResponse(call: Call<AddBalanceResponse>, response: Response<AddBalanceResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    clientSecret = response.body()?.client_secret
                    if (clientSecret != null) {
                        presentPaymentSheet()
                    } else {
                        Toast.makeText(this@AddBalanceActivity, "Error retrieving payment info", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AddBalanceActivity, "Failed to add balance: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddBalanceResponse>, t: Throwable) {
                Toast.makeText(this@AddBalanceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun presentPaymentSheet() {
        val configuration = PaymentSheet.Configuration("Your App Name")
        paymentSheet.presentWithPaymentIntent(clientSecret!!, configuration)
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
                fetchUpdatedBalance() // Fetch and update the balance after successful payment
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment failed: ${paymentSheetResult.error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUpdatedBalance() {
        val request = TokenRequest(sessionToken)
        RetrofitClient.apiService.getBalance(request).enqueue(object : Callback<BalanceResponse> {
            override fun onResponse(call: Call<BalanceResponse>, response: Response<BalanceResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val updatedBalance = response.body()?.balance ?: 0.0
                    Toast.makeText(this@AddBalanceActivity, "Updated Balance: $%.2f".format(updatedBalance), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddBalanceActivity, "Failed to fetch updated balance.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                Toast.makeText(this@AddBalanceActivity, "Error fetching updated balance: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
