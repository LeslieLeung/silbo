package com.ameow.silbo.ui.contact

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ameow.silbo.R
import com.ameow.silbo.SilboApplication
import com.ameow.silbo.SilboApplication.Companion.context
import com.ameow.silbo.logic.model.Contact
import com.ameow.silbo.ui.chat.ChatActivity

class ContactAdapter(val contactList: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactImage: ImageView = view.findViewById(R.id.contactImage)
        val contactName: TextView = view.findViewById(R.id.contactName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val contact = contactList[position]
            val intent = Intent(SilboApplication.context, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactList[position]
        holder.contactImage.setImageResource(contact.contactImg)
        holder.contactName.text = contact.name
    }

    override fun getItemCount(): Int = contactList.size
}