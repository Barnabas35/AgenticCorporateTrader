package com.example.act_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHelper {

    // Method to set up the bottom navigation
    fun setupBottomNavigation(activity: AppCompatActivity, selectedItemId: Int) {
        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (selectedItemId != -1) {
            bottomNavigationView.selectedItemId = selectedItemId
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
