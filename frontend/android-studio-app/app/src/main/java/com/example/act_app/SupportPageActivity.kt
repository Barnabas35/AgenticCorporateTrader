package com.example.act_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SupportPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supportpage)

        // Set up the bottom navigation and pass -1 since SupportPageActivity has no associated nav item
        NavigationHelper.setupBottomNavigation(this, -1)

        // Find the bottom navigation view
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Explicitly deselect all menu items
        bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.menu.getItem(i).isChecked = false
        }
    }
}
