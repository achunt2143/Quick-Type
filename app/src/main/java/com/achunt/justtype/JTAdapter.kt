package com.achunt.justtype

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class JTAdapter(c: Context, q: String) :
    RecyclerView.Adapter<JTAdapter.ViewHolder>(), Filterable {
    init {
        val pm = c.packageManager
        jtList = ArrayList<AppInfo?>()
        jtListNew = ArrayList<AppInfo?>()
        jtListAll = ArrayList<AppInfo?>()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = pm.queryIntentActivities(i, 0)
        if (q.isEmpty()) {
            for (ri in allApps) {
                val app = AppInfo()
                app.label = ri.loadLabel(pm)
                app.packageName = ri.activityInfo.packageName
                app.icon = ri.activityInfo.loadIcon(pm)
                jtList!!.add(app)
            }
            jtListAll!!.addAll(jtList!!)
            try {
                (jtList as ArrayList<AppInfo?>).sortWith(
                    Comparator.comparing<AppInfo, _> { o: AppInfo -> o.label.toString() }
                )
            } catch (e: Exception) {
                Log.d("Error", e.toString())
            }
        } else {
            query = q
            for (ri in allApps) {
                val app = AppInfo()
                app.label = ri.loadLabel(pm)
                app.packageName = ri.activityInfo.packageName
                app.icon = ri.activityInfo.loadIcon(pm)
                if (app.label.toString().lowercase(Locale.getDefault()).startsWith(q)) {
                    jtList!!.add(app)
                }
            }
            jtListAll!!.addAll(jtList!!)
            try {
                (jtList as ArrayList<AppInfo?>).sortWith(
                    Comparator.comparing<AppInfo, _> { o: AppInfo -> o.label.toString() }
                )
            } catch (e: Exception) {
                Log.d("Error", e.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return jtList!!.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        try {
            val appLabel: String = jtList!![i]?.label.toString()
            val appIcon: Drawable? = jtList!![i]?.icon
            val textView = viewHolder.textView
            textView.text = appLabel
            val imageView = viewHolder.img
            imageView.setImageDrawable(appIcon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.jt_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    jtListNew = jtList
                } else {
                    val filteredList: MutableList<AppInfo?> = ArrayList<AppInfo?>()
                    for (c in jtListAll!!) {
                        if (c != null) {
                            if (c.label.toString().lowercase(Locale.getDefault())
                                    .startsWith(charString.lowercase(Locale.getDefault()))
                            ) {
                                filteredList.add(c)
                            }
                        }
                    }
                    jtListNew = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = jtListNew
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                jtList!!.clear()
                jtList!!.addAll((filterResults.values as Collection<AppInfo?>))
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView
        var img: ImageView

        init {
            textView = itemView.findViewById(R.id.jt_app_name)
            img = itemView.findViewById(R.id.jt_app_icon)
            itemView.setOnClickListener { v: View ->
                val pos = adapterPosition
                val context = v.context
                val launchIntent = context.packageManager.getLaunchIntentForPackage(
                jtList!![pos]?.packageName.toString()
                )
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        var jtList: MutableList<AppInfo?>? = null
        var jtListNew: List<AppInfo?>? = null
        var jtListAll: MutableList<AppInfo?>? = null
        var query: String = ""
    }
}
