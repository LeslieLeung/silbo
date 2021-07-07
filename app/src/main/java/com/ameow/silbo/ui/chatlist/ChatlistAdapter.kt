package com.ameow.silbo.ui.chatlist

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.ameow.silbo.R
import com.ameow.silbo.SilboApplication
import com.ameow.silbo.SilboApplication.Companion.context
import com.ameow.silbo.logic.model.Chat
import com.ameow.silbo.ui.chat.ChatActivity

class ChatlistAdapter(val chatlist: List<Chat>):
    RecyclerView.Adapter<ChatlistAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val chatImage: ImageView = view.findViewById(R.id.chatImage)
            val chatTitle: TextView = view.findViewById(R.id.chatTitle)
            val chatContent: TextView = view.findViewById(R.id.chatContent)
            val chatTime: TextView = view.findViewById(R.id.chatTime)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val chat = chatlist[position]
            val intent = Intent(SilboApplication.context, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatlist[position]
        holder.chatImage.setImageResource(chat.chatImg)
        holder.chatTitle.text = chat.title
        holder.chatContent.text = chat.content
        holder.chatTime.text = chat.time
    }

    override fun getItemCount(): Int {
        return chatlist.size
    }
}