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
    var isCall: Boolean,
    contacts: List<Contact>
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>(), Filterable {

    private var filteredList: MutableList<Contact> = contacts.sortedBy { it.name }.toMutableList()
    private val fullList: List<Contact> = filteredList.toList()

    override fun getItemCount(): Int = filteredList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (position in filteredList.indices) {
            val contact = filteredList[position]

            // Set the contact name and number
            viewHolder.nameTextView.text = contact.name.ifEmpty { "Unknown Name" }
            viewHolder.numberTextView.text = formatPhoneNumber(contact.number)

            // Set the contact photo if available
            contact.photoUri?.let {
                viewHolder.img.visibility = View.VISIBLE
                viewHolder.img.setImageURI(it)
            } ?: run {
                viewHolder.img.visibility = View.INVISIBLE
            }
        } else {
            viewHolder.nameTextView.text = "No contact found"
            viewHolder.numberTextView.text = ""
            viewHolder.img.visibility = View.INVISIBLE
        }
    }


    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            val query = charSequence?.toString()?.lowercase(Locale.getDefault()) ?: ""
            val result = if (query.isEmpty()) {
                fullList
            } else {
                fullList.filter {
                    it.name.lowercase(Locale.getDefault()).contains(query) ||
                            it.number.contains(query)
                }
            }
            return FilterResults().apply { values = result }
        }

        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            filteredList = (filterResults?.values as? List<Contact>)?.toMutableList() ?: mutableListOf()
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.contact_name)
        val numberTextView: TextView = itemView.findViewById(R.id.contact_number)
        val img: ImageView = itemView.findViewById(R.id.contact_icon)

        init {
            itemView.setOnClickListener {
                val contact = filteredList[adapterPosition]
                val context = itemView.context

                if (isCall) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.number}")
                    }
                    context.startActivity(intent)
                } else {
                    val textArray = JustType.textSend.split(" ")
                    val messageBody = textArray.drop(2).joinToString(" ").trim()
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.number}")).apply {
                        putExtra("sms_body", messageBody)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }


    private fun formatPhoneNumber(phoneNumber: String): String {
        val cleaned = phoneNumber.filter { it.isDigit() } // Ensure only digits are processed
        return when {
            cleaned.length == 10 -> "(${cleaned.substring(0, 3)}) ${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
            cleaned.length == 11 && cleaned.startsWith("1") -> "+1 (${cleaned.substring(1, 4)}) ${cleaned.substring(4, 7)}-${cleaned.substring(7)}"
            else -> phoneNumber // Return original if the format isn't standard
        }
    }

}
