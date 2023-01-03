package com.achunt.justtype

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList


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
        println("cName size ${cName.size}")
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
                println("start filter clistall size ${cListAll.size}")
                if (charString.isEmpty()) {
                    println("is empty")
                    cListNameNew = cList
                } else {
                    println("else")
                    val filteredList: MutableList<String> = ArrayList()
                    for (c in cListAll) {
                        //println("printing c $c")
                        if (c.lowercase(Locale.getDefault())
                                .startsWith(charString.lowercase(Locale.getDefault()))
                        ) {
                           // println("add all")
                            filteredList.add(c)
                        }
                    }
                    cListNameNew = filteredList
                }
                //cList.clear();
                val filterResults = FilterResults()
                filterResults.values = cListNameNew
                println("return")
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                cList.clear()
                notifyDataSetChanged()
                cList.addAll((filterResults.values as ArrayList<String>))
                println("notify set change")
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

        init {
            textView = itemView.findViewById(R.id.jt_app_name)
            img = itemView.findViewById(R.id.jt_app_icon)
            val et = itemView.findViewById<EditText>(R.id.jtInput)
            if (isCall) {
                itemView.setOnClickListener { v: View ->
                    val split =
                        textView.text.toString().replace("[^0-9]".toRegex(), "")
                    val context = v.context
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:" + split/*cList[adapterPosition]split[1] + split[2]*/)
                    context.startActivity(intent)
                }
            } else {
                itemView.setOnClickListener { v: View ->
                    val text =
                        et.text.toString().split(" ").toTypedArray()
                    var toSend = ""
                    for (i in 5 until text.size) {
                        toSend += text[i]
                    }
                    val split =
                        textView.text.toString().split(" ").toTypedArray()
                    val context = v.context
                    val uri = Uri.parse("smsto:" + split[1])
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    intent.putExtra("sms_body", toSend)
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
