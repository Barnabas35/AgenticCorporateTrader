package com.tradeagently.act_app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for RecyclerView to display stock items
class StockAdapter(private val stockList: List<StockItem>) : RecyclerView.Adapter<StockAdapter.StockViewHolder>() {

    // Creates and inflates the view for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view)
    }

    // Binds data to each view item in the RecyclerView
    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.symbolTextView.text = stock.symbol
        holder.companyNameTextView.text = stock.company_name
        holder.priceTextView.text = "${stock.price} ${stock.currency}"

        // Set up a click listener to open StockProfileActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, StockProfileActivity::class.java).apply {
                putExtra("stock_symbol", stock.symbol)
                putExtra("stock_company_name", stock.company_name)
                putExtra("stock_price", stock.price)
                putExtra("stock_currency", stock.currency)
            }
            context.startActivity(intent)
        }
    }

    // Returns the total number of items
    override fun getItemCount(): Int = stockList.size

    // ViewHolder class that holds references to each view in an item layout
    inner class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val symbolTextView: TextView = view.findViewById(R.id.stockSymbol)
        val companyNameTextView: TextView = view.findViewById(R.id.stockCompanyName)
        val priceTextView: TextView = view.findViewById(R.id.stockPrice)
    }
}
