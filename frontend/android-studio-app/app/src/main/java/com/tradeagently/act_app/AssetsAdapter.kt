package com.tradeagently.act_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssetsAdapter(private var assets: List<String>) : RecyclerView.Adapter<AssetsAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val assetName: TextView = view.findViewById(R.id.assetName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.asset_item, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.assetName.text = assets[position]
    }

    override fun getItemCount() = assets.size

    fun updateAssets(newAssets: List<String>) {
        assets = newAssets
        notifyDataSetChanged()
    }
}

