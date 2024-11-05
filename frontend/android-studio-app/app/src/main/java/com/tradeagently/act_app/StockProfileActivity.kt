package com.tradeagently.act_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StockProfileActivity : AppCompatActivity() {

    private lateinit var companyNameTextView: TextView
    private lateinit var symbolTextView: TextView
    private lateinit var closePriceTextView: TextView
    private lateinit var changePercentageTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var highPriceTextView: TextView
    private lateinit var lowPriceTextView: TextView
    private lateinit var openPriceTextView: TextView
    private lateinit var volumeTextView: TextView
    private lateinit var homepageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_profile)

        // Set up the bottom navigation
        NavigationHelper.setupBottomNavigation(this, -1)

        // Initialize TextViews
        companyNameTextView = findViewById(R.id.companyNameTextView)
        symbolTextView = findViewById(R.id.symbolTextView)
        closePriceTextView = findViewById(R.id.closePriceTextView)
        changePercentageTextView = findViewById(R.id.changePercentageTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        highPriceTextView = findViewById(R.id.highPriceTextView)
        lowPriceTextView = findViewById(R.id.lowPriceTextView)
        openPriceTextView = findViewById(R.id.openPriceTextView)
        volumeTextView = findViewById(R.id.volumeTextView)
        homepageTextView = findViewById(R.id.homepageTextView)

        // Display stock information
        displayStockInfoFromIntent()
    }

    private fun displayStockInfoFromIntent() {
        val companyName = intent.getStringExtra("company_name")
        val symbol = intent.getStringExtra("symbol")
        val closePrice = intent.getDoubleExtra("close_price", 0.0)
        val changePercentage = intent.getDoubleExtra("change_percentage", 0.0)
        val description = intent.getStringExtra("company_description")
        val highPrice = intent.getDoubleExtra("high_price", 0.0)
        val lowPrice = intent.getDoubleExtra("low_price", 0.0)
        val openPrice = intent.getDoubleExtra("open_price", 0.0)
        val volume = intent.getDoubleExtra("volume", 0.0)
        val homepage = intent.getStringExtra("homepage")

        // Set data to TextViews
        symbolTextView.text = symbol ?: "N/A"
        companyNameTextView.text = companyName ?: "N/A"
        closePriceTextView.text = "Close Price: ${if (closePrice != 0.0) String.format("%.2f", closePrice) else "N/A"}"
        changePercentageTextView.text = "Change Percentage: ${String.format("%.4f%%", changePercentage)}"
        descriptionTextView.text = description ?: "No description available"
        highPriceTextView.text = "High Price: ${if (highPrice != 0.0) String.format("%.2f", highPrice) else "N/A"}"
        lowPriceTextView.text = "Low Price: ${if (lowPrice != 0.0) String.format("%.2f", lowPrice) else "N/A"}"
        openPriceTextView.text = "Open Price: ${if (openPrice != 0.0) String.format("%.2f", openPrice) else "N/A"}"
        volumeTextView.text = "Volume: ${if (volume != 0.0) String.format("%.0f", volume) else "N/A"}"
        homepageTextView.text = homepage ?: "No homepage available"
    }

}
