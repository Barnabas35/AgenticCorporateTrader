package com.example.act_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MyAssetsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_assets)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set the selected item to highlight MyAssets when this activity is active
        bottomNavigationView.selectedItemId = R.id.nav_my_assets

        // Set navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_my_assets -> {
                    // Already in MyAssetsActivity, do nothing
                    true
                }
                R.id.nav_stock_activity -> {
                    // Navigate to StockActivity
                    val intent = Intent(this, StockActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_menu -> {
                    // Handle burger menu logic
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
