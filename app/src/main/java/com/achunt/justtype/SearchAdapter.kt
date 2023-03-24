package com.achunt.justtype

import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class SearchAdapter(q: MutableList<String>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>(), Filterable {
    init {
        query = q
    }

    override fun getItemCount(): Int {
        return query.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        try {
            viewHolder.textViewWeb.text = query[i]
            when (i) {
                0 -> {
                    val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("http://"))
                    val resolveInfo = viewHolder.itemView.context.packageManager.resolveActivity(
                        browserIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                    viewHolder.imgWeb.setImageDrawable(
                        resolveInfo!!.activityInfo.applicationInfo.loadIcon(
                            viewHolder.itemView.context.packageManager
                        )
                    )
                }
                1 -> {
                    viewHolder.imgWeb.setImageResource(R.drawable.map)
                }
                2 -> {
                    viewHolder.imgWeb.setImageResource(R.drawable.youtube)
                }
                3 -> {
                    viewHolder.imgWeb.setImageResource(R.drawable.wiki)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.web_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewWeb: TextView
        var imgWeb: ImageView

        init {
            textViewWeb = itemView.findViewById(R.id.jt_web_name)
            imgWeb = itemView.findViewById(R.id.jt_web_icon)
            itemView.setOnClickListener { v: View ->
                when (adapterPosition) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_WEB_SEARCH)
                        intent.putExtra(SearchManager.QUERY, query[adapterPosition])
                        v.context.startActivity(intent)
                    }
                    1 -> {
                        try {
                            val geo = Uri.parse("geo:0,0?q=${query[adapterPosition]}")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = geo
                            v.context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_WEB_SEARCH)
                            intent.putExtra(SearchManager.QUERY, query[adapterPosition])
                            v.context.startActivity(intent)
                        }
                    }
                    2 -> {
                        try {
                            val intent = Intent(Intent.ACTION_SEARCH)
                            intent.`package` = "com.google.android.youtube"
                            intent.putExtra("query", query[adapterPosition])
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            v.context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Intent.ACTION_WEB_SEARCH)
                            intent.putExtra(SearchManager.QUERY, query[adapterPosition])
                            v.context.startActivity(intent)
                        }
                    }
                    3 -> {
                        try {
                            val intent = Intent(Intent.ACTION_SEARCH)
                            intent.`package` = "org.wikipedia"
                            intent.putExtra("query", query[adapterPosition])
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            v.context.startActivity(intent)
                        } catch (e: Exception) {
                            val url =
                                "https://en.wikipedia.org/wiki/Special:Search?search=${query[adapterPosition]}"
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            v.context.startActivity(i)
                        }
                    }
                }
            }
        }
    }

    companion object {
        var query: MutableList<String> = mutableListOf()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                val filteredList: MutableList<String> = ArrayList<String>()
                if (charString.isEmpty()) {
                    filteredList.addAll(query)
                } else {
                    for (c in query) {
                        if (c.lowercase(Locale.getDefault())
                                .startsWith(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(c)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                query.clear()
                for (i in 0..3) {
                    query.add(i, charSequence as String)
                }
                notifyDataSetChanged()
            }
        }
    }
}
