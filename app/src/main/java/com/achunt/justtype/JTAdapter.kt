package com.achunt.justtype

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class JTAdapter(private val context: Context) :
    ListAdapter<AppInfo, JTAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.jt_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)
        holder.appName.text = app.label
        if (app.icon != null) {
            holder.appIcon.setImageDrawable(app.icon)
        } else {
            holder.appIcon.setImageResource(R.drawable.ic_launcher_foreground)
        }

        val clickListener = View.OnClickListener {
            AppRepository.recordLaunch(context, app.packageName)
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                try {
                    context.startActivity(launchIntent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Could not launch ${app.label}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Cannot launch ${app.label}", Toast.LENGTH_SHORT).show()
            }
        }
        holder.card.setOnClickListener(clickListener)
        holder.itemView.setOnClickListener(clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.app_card)
        val appName: TextView = itemView.findViewById(R.id.jt_app_name)
        val appIcon: ImageView = itemView.findViewById(R.id.jt_app_icon)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem.packageName == newItem.packageName

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
            oldItem.label == newItem.label && oldItem.launchCount == newItem.launchCount
    }
}
