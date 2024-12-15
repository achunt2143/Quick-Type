package com.achunt.justtype

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class JTAdapter(private val context: Context) :
    RecyclerView.Adapter<JTAdapter.ViewHolder>(), Filterable {

    private var jtList: MutableList<AppInfo?> = ArrayList()
    private var jtListAll: MutableList<AppInfo?> = ArrayList()
    private var jtListNew: MutableList<AppInfo?> = ArrayList()

    init {
        loadAllApps()
    }

    private fun loadAllApps() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = pm.queryIntentActivities(intent, 0)
        for (ri in allApps) {
            val app = AppInfo()
            app.label = ri.loadLabel(pm)
            app.packageName = ri.activityInfo.packageName
            app.icon = ri.activityInfo.loadIcon(pm)
            jtListAll.add(app)
        }
        jtList.addAll(jtListAll)
        jtList.sortBy { it?.label.toString() }
    }

    override fun getItemCount(): Int {
        return jtList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        try {
            val app = jtList[position]
            viewHolder.jtAppName.text = app?.label
            viewHolder.jtAppIcon.setImageDrawable(app?.icon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.jt_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString().lowercase(Locale.getDefault())
                jtListNew = jtListAll.filter {
                    it?.label?.toString()?.startsWith(charString, ignoreCase = true) == true
                }.toMutableList()

                val filterResults = FilterResults()
                filterResults.values = jtListNew
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                jtList.clear()
                jtList.addAll(filterResults.values as Collection<AppInfo?>)
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var jtAppName: TextView = itemView.findViewById(R.id.jt_app_name)
        var jtAppIcon: ImageView = itemView.findViewById(R.id.jt_app_icon)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                val launchIntent = context.packageManager.getLaunchIntentForPackage(jtList[pos]?.packageName.toString())
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                }
            }
        }
    }
}
