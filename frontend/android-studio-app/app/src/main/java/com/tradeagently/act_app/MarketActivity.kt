package com.tradeagently.act_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
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
    private lateinit var suggestionRecyclerView: RecyclerView
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var stockSearchView: SearchView
    private lateinit var buttonStock: Button
    private lateinit var buttonCrypto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        NavigationHelper.setupBottomNavigation(this, R.id.nav_market)

        // Prevent bottom navigation from moving with keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        stockRecyclerView = findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(this)

        suggestionRecyclerView = findViewById(R.id.suggestionRecyclerView)
        suggestionRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionRecyclerView.visibility = View.GONE // Initially hidden

        stockSearchView = findViewById(R.id.stockSearchView)
        buttonStock = findViewById(R.id.buttonStock)
        buttonCrypto = findViewById(R.id.buttonCrypto)

        // Set initial button states
        buttonStock.isSelected = true
        buttonCrypto.isSelected = false

        // Set click listeners for the buttons
        buttonStock.setOnClickListener {
            setButtonSelected(buttonStock)
            setButtonUnselected(buttonCrypto)
            loadTopStocks() // Load stocks when "STOCK" button is selected
        }

        buttonCrypto.setOnClickListener {
            setButtonSelected(buttonCrypto)
            setButtonUnselected(buttonStock)
            loadTopCryptos() // Load cryptos when "CRYPTO" button is selected
        }

        loadTopStocks() // Load stocks by default

        setupSearchView()
    }

    // Function to style the selected button
    private fun setButtonSelected(button: Button) {
        button.isSelected = true
    }

    // Function to style the unselected button
    private fun setButtonUnselected(button: Button) {
        button.isSelected = false
    }

    // Load the top stocks
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

    private fun loadTopCryptos() {
        Toast.makeText(this, "Loading top cryptos (not implemented)", Toast.LENGTH_SHORT).show()
    }

    private fun setupSearchView() {
        stockSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performStockSearch(it)
                    suggestionRecyclerView.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    suggestionRecyclerView.visibility = View.GONE
                } else {
                    fetchSuggestions(newText)
                }
                return true
            }
        })
    }

    private fun fetchSuggestions(query: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null) ?: return

        val searchRequest = SearchRequest(
            search_query = query,
            limit = 3,
            session_token = token,
            show_price = false
        )

        RetrofitClient.apiService.searchStocks(searchRequest).enqueue(object : Callback<StockSearchResponse> {
            override fun onResponse(call: Call<StockSearchResponse>, response: Response<StockSearchResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { stockSearchResponse ->
                        if (stockSearchResponse.status == "Success") {
                            showSuggestions(stockSearchResponse.ticker_details.take(3))
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

    private fun showSuggestions(suggestions: List<StockItem>) {
        if (suggestions.isNotEmpty()) {
            suggestionAdapter = SuggestionAdapter(suggestions) { suggestion ->
                performStockSearch(suggestion.symbol)
                suggestionRecyclerView.visibility = View.GONE
            }
            suggestionRecyclerView.adapter = suggestionAdapter
            suggestionRecyclerView.visibility = View.VISIBLE
        } else {
            suggestionRecyclerView.visibility = View.GONE
        }
    }

    private fun performStockSearch(query: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null) ?: return

        val searchRequest = SearchRequest(
            search_query = query,
            limit = 50,
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

    private fun displayStocks(stocks: List<StockItem>) {
        stockAdapter = StockAdapter(stocks)
        stockRecyclerView.adapter = stockAdapter
    }
}
