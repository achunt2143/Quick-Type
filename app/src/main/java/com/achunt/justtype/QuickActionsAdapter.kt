package com.achunt.justtype

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class QuickActionsAdapter :
    ListAdapter<QuickAction, QuickActionsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.action_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = getItem(position)
        holder.icon.setImageResource(action.iconRes)
        holder.title.text = action.title
        holder.subtitle.text = action.subtitle
        holder.trailingIcon.visibility = View.GONE

        val clickListener = View.OnClickListener {
            action.execute(it.context)
        }
        holder.card.setOnClickListener(clickListener)
        holder.itemView.setOnClickListener(clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.action_card)
        val icon: ImageView = itemView.findViewById(R.id.action_icon)
        val title: TextView = itemView.findViewById(R.id.action_title)
        val subtitle: TextView = itemView.findViewById(R.id.action_subtitle)
        val trailingIcon: ImageView = itemView.findViewById(R.id.action_trailing_icon)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<QuickAction>() {
        override fun areItemsTheSame(oldItem: QuickAction, newItem: QuickAction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: QuickAction, newItem: QuickAction): Boolean =
            oldItem.title == newItem.title && oldItem.subtitle == newItem.subtitle
    }
}
