package com.tradeagently.act_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MarketActivity : AppCompatActivity() {

    private lateinit var stockRecyclerView: RecyclerView
    private lateinit var stockAdapter: StockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        NavigationHelper.setupBottomNavigation(this, R.id.nav_market)

        stockRecyclerView = findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(this)

        loadTopStocks()
    }

    private fun loadTopStocks() {
        RetrofitClient.apiService.getTopStocks(limit = 10).enqueue(object : Callback<TopStocksResponse> {
            override fun onResponse(call: Call<TopStocksResponse>, response: Response<TopStocksResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { topStocksResponse ->
                        if (topStocksResponse.status == "Success") {
                            displayStocks(topStocksResponse.ticker_details)
                        } else {
                            Log.e("MarketActivity", "API returned error: ${topStocksResponse.status}")
                        }
                    }
                } else {
                    Log.e("MarketActivity", "Response failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TopStocksResponse>, t: Throwable) {
                Log.e("MarketActivity", "Network error: ${t.message}", t)
            }
        })
    }

    private fun displayStocks(stocks: List<StockItem>) {
        stockAdapter = StockAdapter(stocks)
        stockRecyclerView.adapter = stockAdapter
    }
}
