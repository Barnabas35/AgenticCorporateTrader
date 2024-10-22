package com.example.act_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHelper {

    // Method to set up the bottom navigation
    fun setupBottomNavigation(activity: AppCompatActivity, selectedItemId: Int) {
        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // If selectedItemId is valid and exists in the menu, select it, otherwise set a hidden dummy item as selected
        if (selectedItemId != -1) {
            bottomNavigationView.selectedItemId = selectedItemId
        } else {
            // Select a hidden item (e.g., nav_dummy) to ensure no visible items are selected
            bottomNavigationView.selectedItemId = R.id.nav_dummy
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_my_assets -> {
                    if (selectedItemId != R.id.nav_my_assets) {
                        val intent = Intent(activity, MyAssetsActivity::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_stock -> {
                    if (selectedItemId != R.id.nav_stock) {
                        val intent = Intent(activity, StockActivity::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                R.id.nav_menu -> {
                    if (selectedItemId != R.id.nav_menu) {
                        val intent = Intent(activity, MenuActivity::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                        activity.finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
