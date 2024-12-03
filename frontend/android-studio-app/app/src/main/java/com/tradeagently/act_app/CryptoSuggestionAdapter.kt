package com.tradeagently.act_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CryptoSuggestionAdapter(
    private val suggestions: List<CryptoItem>,
    private val onSuggestionClick: (CryptoItem) -> Unit
) : RecyclerView.Adapter<CryptoSuggestionAdapter.SuggestionViewHolder>() {

    inner class SuggestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val symbolText: TextView = view.findViewById(R.id.symbolText)
        val nameText: TextView = view.findViewById(R.id.cryptoName)
        val priceText: TextView = view.findViewById(R.id.cryptoPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion_crypto, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.symbolText.text = suggestion.symbol
        holder.nameText.text = suggestion.name
        holder.priceText.text = suggestion.price?.let { "Price: $${String.format("%.6f", it)}" }

        holder.itemView.setOnClickListener {
            onSuggestionClick(suggestion)
        }
    }

    override fun getItemCount(): Int = suggestions.size
}
