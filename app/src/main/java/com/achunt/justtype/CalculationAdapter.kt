package com.achunt.justtype

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CalculationAdapter : RecyclerView.Adapter<CalculationAdapter.ViewHolder>() {

    var calculation: CalculationResult? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = if (calculation != null) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.action_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calc = calculation ?: return
        holder.icon.setImageResource(R.drawable.ic_calculate)
        holder.title.text = "= ${calc.result}"
        holder.subtitle.text = calc.expression
        holder.trailingIcon.visibility = View.VISIBLE
        holder.trailingIcon.setImageResource(R.drawable.ic_content_copy)

        val clickListener = View.OnClickListener {
            val context = it.context
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Calculation Result", calc.result)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied ${calc.result} to clipboard", Toast.LENGTH_SHORT).show()
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
}
