package com.achunt.justtype

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class WebSearchItem(
    val engine: WebSearchEngine,
    val query: String
)

class SearchAdapter : ListAdapter<WebSearchItem, SearchAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.web_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.icon.setImageResource(item.engine.iconRes)
        holder.name.text = item.engine.name
        holder.queryPreview.text = "Search for \"${item.query}\""

        val clickListener = View.OnClickListener {
            item.engine.executeSearch(it.context, item.query)
        }
        holder.card.setOnClickListener(clickListener)
        holder.itemView.setOnClickListener(clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.web_card)
        val icon: ImageView = itemView.findViewById(R.id.jt_web_icon)
        val name: TextView = itemView.findViewById(R.id.jt_web_name)
        val queryPreview: TextView = itemView.findViewById(R.id.jt_web_query_preview)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WebSearchItem>() {
        override fun areItemsTheSame(oldItem: WebSearchItem, newItem: WebSearchItem): Boolean =
            oldItem.engine.id == newItem.engine.id

        override fun areContentsTheSame(oldItem: WebSearchItem, newItem: WebSearchItem): Boolean =
            oldItem.query == newItem.query && oldItem.engine.name == newItem.engine.name
    }
}
