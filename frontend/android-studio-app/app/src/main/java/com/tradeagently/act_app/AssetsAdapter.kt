import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tradeagently.act_app.R

class AssetsAdapter(
    private var assets: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AssetsAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val assetName: TextView = view.findViewById(R.id.assetName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.asset_item, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        holder.assetName.text = asset

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(asset)
        }
    }

    override fun getItemCount() = assets.size

    fun updateAssets(newAssets: List<String>) {
        assets = newAssets
        notifyDataSetChanged()
    }
}

