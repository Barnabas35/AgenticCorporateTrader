package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StockAdapter(
    private val stockList: List<StockItem>,
    private val apiService: ApiService,
    private val sessionToken: String
) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.symbolTextView.text = stock.symbol
        holder.companyNameTextView.text = stock.company_name
        holder.priceTextView.text = "${stock.price} ${stock.currency}"

        holder.itemView.setOnClickListener {
            fetchTickerInfo(holder.itemView.context, stock.symbol)
        }
    }

    override fun getItemCount(): Int = stockList.size

    private fun fetchTickerInfo(context: Context, ticker: String) {
        val tickerRequest = TickerRequest(ticker = ticker, session_token = sessionToken)

        apiService.getTickerInfo(tickerRequest).enqueue(object : Callback<TickerInfoResponse> {
            override fun onResponse(call: Call<TickerInfoResponse>, response: Response<TickerInfoResponse>) {
                if (response.isSuccessful && response.body()?.ticker_info != null) {
                    val tickerInfo = response.body()?.ticker_info!!
                    launchStockProfileActivity(context, tickerInfo)
                } else {
                    Toast.makeText(context, "Failed to fetch ticker info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TickerInfoResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("StockAdapter", "Failed to fetch ticker info", t)
            }
        })
    }

    private fun launchStockProfileActivity(context: Context, tickerInfo: TickerInfo) {
        val intent = Intent(context, StockProfileActivity::class.java).apply {
            putExtra("company_name", tickerInfo.company_name)
            putExtra("symbol", tickerInfo.symbol)
            putExtra("close_price", tickerInfo.close_price)
            putExtra("change_percentage", tickerInfo.change_percentage)
            putExtra("company_description", tickerInfo.company_description)
            putExtra("high_price", tickerInfo.high_price)
            putExtra("low_price", tickerInfo.low_price)
            putExtra("open_price", tickerInfo.open_price)
            putExtra("volume", tickerInfo.volume)
            putExtra("currency", tickerInfo.currency)
            putExtra("homepage", tickerInfo.homepage)
        }
        context.startActivity(intent)

        if (context is AppCompatActivity) {
            context.overridePendingTransition(0, 0)
        }
    }

    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val symbolTextView: TextView = view.findViewById(R.id.stockSymbol)
        val companyNameTextView: TextView = view.findViewById(R.id.stockCompanyName)
        val priceTextView: TextView = view.findViewById(R.id.stockPrice)
    }
}
