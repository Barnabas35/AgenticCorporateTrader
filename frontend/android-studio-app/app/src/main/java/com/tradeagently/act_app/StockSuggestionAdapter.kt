package com.tradeagently.act_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockSuggestionAdapter(
    private val suggestions: List<StockItem>,
    private val onSuggestionClick: (StockItem) -> Unit
) : RecyclerView.Adapter<StockSuggestionAdapter.SuggestionViewHolder>() {

    inner class SuggestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val symbolText: TextView = view.findViewById(R.id.symbolText)
        val companyNameText: TextView = view.findViewById(R.id.companyName)
        val bottomDivider: View = view.findViewById(R.id.bottomDivider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.`item_suggestion_stock`, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.symbolText.text = suggestion.symbol
        holder.companyNameText.text = suggestion.company_name
        holder.itemView.setOnClickListener {
            onSuggestionClick(suggestion)
        }

        // Only show the bottom divider except for the last item
        holder.bottomDivider.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int = suggestions.size
}
