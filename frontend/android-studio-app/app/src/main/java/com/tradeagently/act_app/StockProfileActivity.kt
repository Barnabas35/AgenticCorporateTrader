package com.tradeagently.act_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StockProfileActivity : AppCompatActivity() {

    private lateinit var buyButton: Button
    private lateinit var sellButton: Button
    private lateinit var purchaseLogLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_profile)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Retrieve stock details from the intent
        val stockSymbol = intent.getStringExtra("stock_symbol") ?: "N/A"
        val stockCompanyName = intent.getStringExtra("stock_company_name") ?: "N/A"
        val previousClose = intent.getDoubleExtra("previous_close", 0.0)
        val ownsStock = intent.getBooleanExtra("owns_stock", false)

        // Initialize UI elements
        findViewById<TextView>(R.id.stockSymbolTextView).text = stockSymbol
        findViewById<TextView>(R.id.stockCompanyNameTextView).text = stockCompanyName
        findViewById<TextView>(R.id.previousCloseTextView).text = "Previous Close: $$previousClose"

        buyButton = findViewById(R.id.buyButton)
        sellButton = findViewById(R.id.sellButton)
        purchaseLogLayout = findViewById(R.id.purchaseLogLayout)

        // Show purchase log if the user owns the stock
        purchaseLogLayout.visibility = if (ownsStock) View.VISIBLE else View.GONE

        // Set up other UI elements as needed
        findViewById<TextView>(R.id.openPriceTextView).text = "Open: 100" // Replace with dynamic data
        findViewById<TextView>(R.id.closePriceTextView).text = "Close: 90" // Replace with dynamic data
        findViewById<TextView>(R.id.highPriceTextView).text = "High: 110" // Replace with dynamic data
        findViewById<TextView>(R.id.lowPriceTextView).text = "Low: 80" // Replace with dynamic data
        findViewById<TextView>(R.id.companyDescriptionTextView).text = "Company: Description..."
    }
}
