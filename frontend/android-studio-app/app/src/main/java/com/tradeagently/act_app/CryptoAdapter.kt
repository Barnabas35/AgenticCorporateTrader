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

class CryptoAdapter(
    private var cryptoList: List<CryptoItem> = emptyList()
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    fun updateData(newCryptoList: List<CryptoItem>) {
        cryptoList = newCryptoList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crypto, parent, false)
        return CryptoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = cryptoList[position]
        holder.symbolTextView.text = crypto.symbol
        holder.nameTextView.text = crypto.name
        holder.priceTextView.text = "Price: ${formatPrice(crypto.price)}"

        holder.itemView.setOnClickListener {
            fetchCryptoInfo(holder.itemView.context, crypto.symbol)
        }
    }

    private fun formatPrice(price: Double?): String {
        return if (price != null) {
            String.format("%.6f", price)
        } else {
            "N/A"
        }
    }


    override fun getItemCount(): Int = cryptoList.size

    private fun fetchCryptoInfo(context: Context, symbol: String) {
        val sessionToken = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("session_token", "") ?: ""

        val cryptoRequest = CryptoInfoRequest(crypto = symbol, session_token = sessionToken)

        RetrofitClient.apiService.getCryptoInfo(cryptoRequest).enqueue(object : Callback<CryptoInfoResponse> {
            override fun onResponse(call: Call<CryptoInfoResponse>, response: Response<CryptoInfoResponse>) {
                if (response.isSuccessful && response.body()?.crypto_info != null) {
                    val cryptoInfo = response.body()?.crypto_info!!
                    launchCryptoProfileActivity(context, cryptoInfo)
                } else {
                    Toast.makeText(context, "Failed to fetch crypto info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CryptoInfoResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CryptoAdapter", "Failed to fetch crypto info", t)
            }
        })
    }

    private fun launchCryptoProfileActivity(context: Context, cryptoInfo: CryptoInfo) {
        val intent = Intent(context, CryptoProfileActivity::class.java).apply {
            putExtra("symbol", cryptoInfo.symbol)
            putExtra("name", cryptoInfo.name)
            putExtra("latest_price", cryptoInfo.latest_price)
            putExtra("description", cryptoInfo.description)
            putExtra("high", cryptoInfo.high)
            putExtra("low", cryptoInfo.low)
            putExtra("volume", cryptoInfo.volume)
            putExtra("open", cryptoInfo.open)
        }
        context.startActivity(intent)
        (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
    }

    inner class CryptoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val symbolTextView: TextView = view.findViewById(R.id.cryptoSymbol)
        val nameTextView: TextView = view.findViewById(R.id.cryptoName)
        val priceTextView: TextView = view.findViewById(R.id.cryptoPrice)
    }
}
