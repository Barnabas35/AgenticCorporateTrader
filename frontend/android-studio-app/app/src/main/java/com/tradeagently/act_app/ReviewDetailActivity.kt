package com.tradeagently.act_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReviewDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewdetail)

        // Set up bottom navigation
        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy) // Use `nav_dummy` if there's no dedicated admin item


        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        val commentTextView: TextView = findViewById(R.id.commentTextView)

        // Get data from intent
        val score = intent.getIntExtra("score", 0)
        val comment = intent.getStringExtra("comment") ?: "No Comment"

        // Set data to TextViews
        scoreTextView.text = "Score: $score"
        commentTextView.text = comment
    }
}
