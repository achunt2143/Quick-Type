package com.achunt.justtype

import android.content.Intent
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


class ContactsAdapter(
    call: Boolean,
    cName: ArrayList<String>,
    cNumber: ArrayList<String>,
    cPhoto: ArrayList<Uri>
) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>(), Filterable {
    var cListUri: MutableList<Uri> = ArrayList()
    var cListNumber: MutableList<String>
    var isCall = false

    init {
        cListName = cName
        cListNameNew = ArrayList()
        cListNumber = cNumber
        cList = ArrayList()
        cListAll = ArrayList()
        for (i in cName.indices) {
            cListUri.add(cPhoto[i])
            cListName.add(cName[i])
            cListNumber.add(cNumber[i])
            cList.add(cName[i] + " " + cNumber[i])
        }
        cList.sort()
        isCall = call
        cListAll.addAll(cList)
    }

    override fun getItemCount(): Int {
        return cList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    cListNameNew = cList
                } else {
                    val filteredList: MutableList<String> = ArrayList()
                    for (c in cListAll) {
                        if (c.lowercase(Locale.getDefault())
                                .startsWith(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(c)
                        }
                    }
                    cListNameNew = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = cListNameNew
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                cList.clear()
                notifyDataSetChanged()
                cList.addAll((filterResults.values as ArrayList<String>))
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if(i>=0) {
            try {
                val cLabel = cList[i]
                val textView = viewHolder.textView
                textView.text = cLabel
                val imageView = viewHolder.img
                imageView.visibility = View.INVISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                val cLabel = "No contact found"
                val textView = viewHolder.textView
                textView.text = cLabel
                val imageView = viewHolder.img
                imageView.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.jt_item_row_layout, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView
        var img: ImageView
        var textField: String

        init {
            textView = itemView.findViewById(R.id.jt_app_name)
            img = itemView.findViewById(R.id.jt_app_icon)
            textField = JustType.textSend
            if (isCall) {
                itemView.setOnClickListener { v: View ->
                    val split =
                        textView.text.toString().replace("[^0-9]".toRegex(), "")
                    val context = v.context
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:" + split)
                    context.startActivity(intent)
                }
            } else {
                itemView.setOnClickListener { v: View ->
                    val split =
                        textView.text.toString().replace("[^0-9]".toRegex(), "")
                    val textToExtract = textField.split((" ").toRegex())
                    var textToSend = ""
                    if (textToExtract.size > 1) {
                        for (i in 2 until textToExtract.size) {
                            textToSend += "${textToExtract[i]} "
                        }
                    }
                    val context = v.context
                    val uri = Uri.parse("smsto:" + split)
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    intent.putExtra("sms_body", textToSend.trim())
                    context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        lateinit var cListName: MutableList<String>
        lateinit var cListNameNew: List<String>
        lateinit var cList: MutableList<String>
        lateinit var cListAll: MutableList<String>
    }
}
