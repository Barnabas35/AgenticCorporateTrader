package com.tradeagently.act_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MarketActivity : AppCompatActivity() {

    private lateinit var stockRecyclerView: RecyclerView
    private lateinit var stockAdapter: StockAdapter
    private lateinit var stockSearchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        NavigationHelper.setupBottomNavigation(this, R.id.nav_market)

        stockRecyclerView = findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(this)

        stockSearchView = findViewById(R.id.stockSearchView)

        loadTopStocks()

        setupSearchView()
    }

    // Load the top stocks initially
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

    // Set up search functionality
    private fun setupSearchView() {
        stockSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performStockSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // Perform a stock search with the given query
    private fun performStockSearch(query: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null) ?: return

        val searchRequest = SearchRequest(
            search_query = query,
            limit = 10,
            session_token = token,
            show_price = true
        )

        RetrofitClient.apiService.searchStocks(searchRequest).enqueue(object : Callback<StockSearchResponse> {
            override fun onResponse(call: Call<StockSearchResponse>, response: Response<StockSearchResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { stockSearchResponse ->
                        if (stockSearchResponse.status == "Success") {
                            displayStocks(stockSearchResponse.ticker_details)
                        } else {
                            Log.e("MarketActivity", "API returned error: ${stockSearchResponse.status}")
                        }
                    }
                } else {
                    Log.e("MarketActivity", "Search response failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<StockSearchResponse>, t: Throwable) {
                Log.e("MarketActivity", "Network error during search: ${t.message}", t)
            }
        })
    }

    // Update the RecyclerView with the stock list
    private fun displayStocks(stocks: List<StockItem>) {
        stockAdapter = StockAdapter(stocks)
        stockRecyclerView.adapter = stockAdapter
    }
}
