package com.tradeagently.act_app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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
    private lateinit var cryptoRecyclerView: RecyclerView
    private lateinit var stockSuggestionRecyclerView: RecyclerView
    private lateinit var cryptoSuggestionRecyclerView: RecyclerView
    private lateinit var stockSuggestionAdapter: StockSuggestionAdapter
    private lateinit var cryptoSuggestionAdapter: CryptoSuggestionAdapter
    private lateinit var stockSearchView: SearchView
    private lateinit var cryptoSearchView: SearchView
    private lateinit var buttonStock: Button
    private lateinit var buttonCrypto: Button
    private lateinit var sessionToken: String
    private lateinit var stockAdapter: StockAdapter
    private lateinit var cryptoAdapter: CryptoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        NavigationHelper.setupBottomNavigation(this, R.id.nav_market)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        stockRecyclerView = findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(this)
        cryptoRecyclerView = findViewById(R.id.cryptoRecyclerView)
        cryptoRecyclerView.layoutManager = LinearLayoutManager(this)
        cryptoRecyclerView.visibility = View.GONE

        stockSuggestionRecyclerView = findViewById(R.id.stockSuggestionRecyclerView)
        stockSuggestionRecyclerView.layoutManager = LinearLayoutManager(this)
        stockSuggestionRecyclerView.visibility = View.GONE

        cryptoSuggestionRecyclerView = findViewById(R.id.cryptoSuggestionRecyclerView)
        cryptoSuggestionRecyclerView.layoutManager = LinearLayoutManager(this)
        cryptoSuggestionRecyclerView.visibility = View.GONE

        stockSearchView = findViewById(R.id.stockSearchView)
        cryptoSearchView = findViewById(R.id.cryptoSearchView)
        buttonStock = findViewById(R.id.buttonStock)
        buttonCrypto = findViewById(R.id.buttonCrypto)

        sessionToken = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("session_token", "") ?: ""

        stockAdapter = StockAdapter(emptyList(), RetrofitClient.apiService, sessionToken)
        cryptoAdapter = CryptoAdapter(emptyList())

        stockRecyclerView.adapter = stockAdapter
        cryptoRecyclerView.adapter = cryptoAdapter

        setButtonSelected(buttonStock, true)
        setButtonSelected(buttonCrypto, false)
        loadTopStocks()

        buttonStock.setOnClickListener {
            setButtonSelected(buttonStock, true)
            setButtonSelected(buttonCrypto, false)
            loadTopStocks()
        }

        buttonCrypto.setOnClickListener {
            setButtonSelected(buttonStock, false)
            setButtonSelected(buttonCrypto, true)
            loadTopCryptos()
        }

        setupStockSearchView()
        setupCryptoSearchView()
    }

    private fun setButtonSelected(button: Button, isSelected: Boolean) {
        button.isSelected = isSelected
        if (isSelected) {
            button.setBackgroundResource(R.drawable.btn_bg)
            button.setTextColor(Color.WHITE)
            if (button == buttonStock) {
                stockRecyclerView.visibility = View.VISIBLE
                cryptoRecyclerView.visibility = View.GONE
                stockSearchView.visibility = View.VISIBLE
                cryptoSearchView.visibility = View.GONE
                stockSuggestionRecyclerView.visibility = View.GONE
                cryptoSuggestionRecyclerView.visibility = View.GONE
            } else {
                stockRecyclerView.visibility = View.GONE
                cryptoRecyclerView.visibility = View.VISIBLE
                stockSearchView.visibility = View.GONE
                cryptoSearchView.visibility = View.VISIBLE
                stockSuggestionRecyclerView.visibility = View.GONE
                cryptoSuggestionRecyclerView.visibility = View.GONE
            }
        } else {
            button.setBackgroundResource(R.drawable.btn_bg)
            button.setTextColor(Color.WHITE)
        }
    }

    private fun loadTopStocks() {
        RetrofitClient.apiService.getTopStocks(10).enqueue(object : Callback<TopStocksResponse> {
            override fun onResponse(call: Call<TopStocksResponse>, response: Response<TopStocksResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val stockItems = response.body()!!.ticker_details
                    stockAdapter = StockAdapter(stockItems, RetrofitClient.apiService, sessionToken)
                    stockRecyclerView.adapter = stockAdapter
                } else {
                    showToast("Error loading stocks")
                }
            }

            override fun onFailure(call: Call<TopStocksResponse>, t: Throwable) {
                showToast("Network error loading stocks")
            }
        })
    }

    private fun loadTopCryptos() {
        RetrofitClient.apiService.getTopCryptos(10).enqueue(object : Callback<TopCryptosResponse> {
            override fun onResponse(call: Call<TopCryptosResponse>, response: Response<TopCryptosResponse>) {
                if (response.isSuccessful && response.body()?.status == "Success") {
                    val cryptoItems = response.body()!!.crypto_details ?: emptyList()
                    cryptoAdapter.updateData(cryptoItems)
                } else {
                    showToast("Error loading cryptos")
                }
            }

            override fun onFailure(call: Call<TopCryptosResponse>, t: Throwable) {
                showToast("Network error loading cryptos")
            }
        })
    }

    private fun setupStockSearchView() {
        stockSearchView.setOnClickListener {
            stockSearchView.isIconified = false
        }
        stockSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performStockSearch(it)
                    stockSuggestionRecyclerView.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    stockSuggestionRecyclerView.visibility = View.GONE
                } else {
                    fetchStockSuggestions(newText)
                }
                return true
            }
        })
    }

    private fun setupCryptoSearchView() {
        cryptoSearchView.setOnClickListener {
            cryptoSearchView.isIconified = false
        }
        cryptoSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performCryptoSearch(it)
                    cryptoSuggestionRecyclerView.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    cryptoSuggestionRecyclerView.visibility = View.GONE
                } else {
                    fetchCryptoSuggestions(newText)
                }
                return true
            }
        })
    }

    private fun fetchStockSuggestions(query: String) {
        val searchRequest = SearchRequest(query, limit = 3, sessionToken, show_price = false)
        RetrofitClient.apiService.searchStocks(searchRequest).enqueue(object : Callback<StockSearchResponse> {
            override fun onResponse(call: Call<StockSearchResponse>, response: Response<StockSearchResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { stockSearchResponse ->
                        if (stockSearchResponse.status == "Success") {
                            stockSuggestionAdapter = StockSuggestionAdapter(stockSearchResponse.ticker_details.take(3)) { suggestion ->
                                performStockSearch(suggestion.symbol)
                                stockSuggestionRecyclerView.visibility = View.GONE
                            }
                            stockSuggestionRecyclerView.adapter = stockSuggestionAdapter
                            stockSuggestionRecyclerView.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onFailure(call: Call<StockSearchResponse>, t: Throwable) {
                showToast("Error fetching stock suggestions")
            }
        })
    }

    private fun fetchCryptoSuggestions(query: String) {
        val searchRequest = TextSearchCryptoRequest(query, limit = 3, sessionToken, show_price = true)
        RetrofitClient.apiService.textSearchCrypto(searchRequest).enqueue(object : Callback<TextSearchCryptoResponse> {
            override fun onResponse(call: Call<TextSearchCryptoResponse>, response: Response<TextSearchCryptoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { cryptoSearchResponse ->
                        if (cryptoSearchResponse.status == "Success") {
                            cryptoSuggestionAdapter = CryptoSuggestionAdapter(cryptoSearchResponse.crypto_details.take(3)) { suggestion ->
                                performCryptoSearch(suggestion.symbol)
                                cryptoSuggestionRecyclerView.visibility = View.GONE
                            }
                            cryptoSuggestionRecyclerView.adapter = cryptoSuggestionAdapter
                            cryptoSuggestionRecyclerView.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TextSearchCryptoResponse>, t: Throwable) {
                showToast("Error fetching crypto suggestions")
            }
        })
    }

    private fun performStockSearch(query: String) {
        val searchRequest = SearchRequest(query, limit = 50, sessionToken, show_price = true)
        RetrofitClient.apiService.searchStocks(searchRequest).enqueue(object : Callback<StockSearchResponse> {
            override fun onResponse(call: Call<StockSearchResponse>, response: Response<StockSearchResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { stockSearchResponse ->
                        if (stockSearchResponse.status == "Success") {
                            stockAdapter = StockAdapter(stockSearchResponse.ticker_details, RetrofitClient.apiService, sessionToken)
                            stockRecyclerView.adapter = stockAdapter
                        }
                    }
                }
            }

            override fun onFailure(call: Call<StockSearchResponse>, t: Throwable) {
                showToast("Error during stock search")
            }
        })
    }

    private fun performCryptoSearch(query: String) {
        val searchRequest = TextSearchCryptoRequest(query, limit = 50, sessionToken, show_price = true)
        RetrofitClient.apiService.textSearchCrypto(searchRequest).enqueue(object : Callback<TextSearchCryptoResponse> {
            override fun onResponse(call: Call<TextSearchCryptoResponse>, response: Response<TextSearchCryptoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { cryptoSearchResponse ->
                        if (cryptoSearchResponse.status == "Success") {
                            cryptoAdapter.updateData(cryptoSearchResponse.crypto_details ?: emptyList())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TextSearchCryptoResponse>, t: Throwable) {
                showToast("Error during crypto search")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
