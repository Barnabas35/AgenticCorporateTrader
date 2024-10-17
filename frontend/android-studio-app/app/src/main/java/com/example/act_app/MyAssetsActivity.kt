package com.example.act_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MyAssetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_assets)

        // Set up the bottom navigation to handle navigation between activities
        NavigationHelper.setupBottomNavigation(this, R.id.nav_my_assets)
    }
}
