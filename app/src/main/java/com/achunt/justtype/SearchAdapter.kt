package com.achunt.justtype

import android.app.SearchManager
import android.content.Context
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class SearchAdapter(context: Context, private var queries: MutableList<String>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>(), Filterable {

    private var filteredQueries: MutableList<String> = queries.toMutableList()
    private val storedBrowserPackage = SharedPreferencesHelper.getString(context, "default_browser")


    override fun getItemCount(): Int = filteredQueries.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val query = filteredQueries[position]
        viewHolder.textViewWeb.text = query

        when (position) {
            0 -> {
                // Get the default browser package name from SharedPreferences
                val defaultBrowserPackageName = storedBrowserPackage

                // If a default browser package name is available, use its icon
                if (!defaultBrowserPackageName.isNullOrEmpty()) {
                    try {
                        val applicationInfo = viewHolder.itemView.context.packageManager.getApplicationInfo(
                            defaultBrowserPackageName, 0
                        )
                        viewHolder.imgWeb.setImageDrawable(applicationInfo.loadIcon(viewHolder.itemView.context.packageManager))
                    } catch (e: PackageManager.NameNotFoundException) {
                        // In case the package name is not found, use a placeholder icon
                        viewHolder.imgWeb.setImageDrawable(AppCompatResources.getDrawable(viewHolder.itemView.context, R.drawable.search))
                    }
                } else {
                    // If no default browser is set, use the placeholder icon
                    viewHolder.imgWeb.setImageDrawable(AppCompatResources.getDrawable(viewHolder.itemView.context, R.drawable.search))

                    // If the package name is not found in SharedPreferences, fetch it in the background
                    CoroutineScope(Dispatchers.IO).launch {
                        // Create an intent to view a URL
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"))

                        // Resolve the intent to get the details of the activity that can handle this intent
                        val resolveInfo = viewHolder.itemView.context.packageManager.resolveActivity(
                            browserIntent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )

                        // If there's a valid resolveInfo, get the package name
                        val packageName = resolveInfo?.activityInfo?.packageName

                        if (packageName != null) {
                            // Set the package name in SharedPreferences
                            SharedPreferencesHelper.saveString(viewHolder.itemView.context, "default_browser", packageName)

                            withContext(Dispatchers.Main) {
                                try {
                                    val applicationInfo = viewHolder.itemView.context.packageManager.getApplicationInfo(
                                        packageName, 0
                                    )
                                    viewHolder.imgWeb.setImageDrawable(applicationInfo.loadIcon(viewHolder.itemView.context.packageManager))
                                } catch (e: PackageManager.NameNotFoundException) {
                                    // In case the package name is not found, use a placeholder icon
                                    viewHolder.imgWeb.setImageDrawable(AppCompatResources.getDrawable(viewHolder.itemView.context, R.drawable.search))
                                }
                            }
                        }
                    }
                }
            }


            1 -> viewHolder.imgWeb.setImageResource(R.drawable.map)
            2 -> viewHolder.imgWeb.setImageResource(R.drawable.youtube)
            3 -> viewHolder.imgWeb.setImageResource(R.drawable.wiki)
            else -> viewHolder.imgWeb.setImageResource(R.drawable.search)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.web_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewWeb: TextView = itemView.findViewById(R.id.jt_web_name)
        val imgWeb: ImageView = itemView.findViewById(R.id.jt_web_icon)

        init {
            itemView.setOnClickListener { v: View ->
                val context = v.context
                val query = filteredQueries[adapterPosition]

                when (adapterPosition) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                            putExtra(SearchManager.QUERY, query)
                        }
                        context.startActivity(intent)
                    }
                    1 -> {
                        val geoUri = Uri.parse("geo:0,0?q=$query")
                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, query)
                            })
                        }
                    }
                    2 -> {
                        try {
                            val intent = Intent(Intent.ACTION_SEARCH).apply {
                                `package` = "com.google.android.youtube"
                                putExtra("query", query)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, query)
                            })
                        }
                    }
                    3 -> {
                        try {
                            val intent = Intent(Intent.ACTION_SEARCH).apply {
                                `package` = "org.wikipedia"
                                putExtra("query", query)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val url =
                                "https://en.wikipedia.org/wiki/Special:Search?search=$query"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }
                    else -> {
                        // Default action for unknown positions
                        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                            putExtra(SearchManager.QUERY, query)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().lowercase(Locale.getDefault())
                val filteredList = if (charString.isEmpty()) {
                    queries
                } else {
                    queries.filter { it.lowercase(Locale.getDefault()).contains(charString) }
                        .toMutableList()
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                filteredQueries = filterResults.values as MutableList<String>
                notifyDataSetChanged()
            }
        }
    }
}

