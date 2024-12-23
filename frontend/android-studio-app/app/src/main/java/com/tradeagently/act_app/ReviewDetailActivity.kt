package com.tradeagently.act_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReviewDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviewdetail)

        NavigationHelper.setupBottomNavigation(this, R.id.nav_dummy)

        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        val commentTextView: TextView = findViewById(R.id.commentTextView)

        val score = intent.getIntExtra("score", 0)
        val comment = intent.getStringExtra("comment") ?: "No comment provided"

        scoreTextView.text = "Score: $score"
        commentTextView.text = comment
    }
}
