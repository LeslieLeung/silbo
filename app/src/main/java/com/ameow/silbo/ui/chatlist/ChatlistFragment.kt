package com.ameow.silbo.ui.chatlist

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ameow.silbo.R
import com.ameow.silbo.logic.model.Chat

class ChatlistFragment : Fragment() {

    companion object {
        val TAG: String = ChatlistFragment::class.java.simpleName
        fun newInstance() = ChatlistFragment()
    }

    private lateinit var viewModel: ChatlistViewModel
    private val chatList = ArrayList<Chat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chatlist_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ChatlistViewModel::class.java)
        // TODO: Use the ViewModel
        initChats()
        val layoutManager = LinearLayoutManager(this.context)
        val recyclerView: RecyclerView = requireView().findViewById(R.id.chatlist)
        recyclerView.layoutManager = layoutManager
        val adapter = ChatlistAdapter(chatList)
        recyclerView.adapter = adapter
    }

    // TODO replace dummy initializer
    private fun initChats() {
        if (chatList.size < 5) {
            repeat(5) {
                chatList.add(Chat("Hello world", R.drawable.ic_avatar,
                    "Hello from android", "13:00"))
            }
        }
    }

}