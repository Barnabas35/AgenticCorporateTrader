package com.tradeagently.act_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tradeagently.act_app.R.layout.activity_add_balance
import com.tradeagently.act_app.RetrofitClient.apiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddBalanceActivity : AppCompatActivity() {

    private lateinit var balanceInput: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_add_balance)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy)

        balanceInput = findViewById(R.id.balanceInput)
        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            val amount = balanceInput.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                addBalance(amount)
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addBalance(amount: Double) {
        val sessionToken = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("session_token", "") ?: ""

        apiService.addBalance(AddBalanceRequest(sessionToken, amount)).enqueue(object :
            Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    Toast.makeText(this@AddBalanceActivity, "Balance added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddBalanceActivity, "Failed to add balance", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@AddBalanceActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
