package com.achunt.justtype

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter : ListAdapter<Contact, ContactsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        holder.nameTextView.text = contact.name.ifEmpty { "Unknown Name" }
        holder.numberTextView.text = formatPhoneNumber(contact.number)

        if (contact.photoUri != null) {
            holder.img.setImageURI(contact.photoUri)
        } else {
            holder.img.setImageResource(R.drawable.shape)
        }

        // Tap on card dials by default
        val dialListener = View.OnClickListener {
            dialNumber(it.context, contact.number)
        }
        holder.card.setOnClickListener(dialListener)
        holder.itemView.setOnClickListener(dialListener)

        // Tap on call button dials
        holder.callButton.setOnClickListener {
            dialNumber(it.context, contact.number)
        }

        // Tap on message button sends SMS
        holder.smsButton.setOnClickListener {
            sendSms(it.context, contact.number)
        }
    }

    private fun dialNumber(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSms(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$number")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        val cleaned = phoneNumber.filter { it.isDigit() }
        return when {
            cleaned.length == 10 -> "(${cleaned.substring(0, 3)}) ${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
            cleaned.length == 11 && cleaned.startsWith("1") -> "+1 (${cleaned.substring(1, 4)}) ${cleaned.substring(4, 7)}-${cleaned.substring(7)}"
            else -> phoneNumber
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: View = itemView.findViewById(R.id.contact_card)
        val nameTextView: TextView = itemView.findViewById(R.id.contact_name)
        val numberTextView: TextView = itemView.findViewById(R.id.contact_number)
        val img: ImageView = itemView.findViewById(R.id.contact_icon)
        val callButton: ImageView = itemView.findViewById(R.id.contact_action_call)
        val smsButton: ImageView = itemView.findViewById(R.id.contact_action_sms)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem.number == newItem.number && oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean =
            oldItem == newItem
    }
}
