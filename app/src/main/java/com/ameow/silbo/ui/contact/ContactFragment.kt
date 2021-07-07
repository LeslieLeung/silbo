package com.ameow.silbo.ui.contact

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ameow.silbo.R
import com.ameow.silbo.logic.model.Contact

class ContactFragment : Fragment() {

    companion object {
        val TAG: String = ContactFragment::class.java.simpleName
        fun newInstance() = ContactFragment()
    }

    private lateinit var viewModel: ContactViewModel
    private val contactList = ArrayList<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.contact_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        // TODO: Use the ViewModel
        initContact()
        val layoutManager = LinearLayoutManager(this.context)
        val recyclerView: RecyclerView = requireView().findViewById(R.id.contactList)
        recyclerView.layoutManager = layoutManager
        val adapter = ContactAdapter(contactList)
        recyclerView.adapter = adapter

    }
    private fun initContact() {
        if (contactList.size < 5) {
            repeat(5) {
                contactList.add(
                    Contact("Ee鹅", R.drawable.ic_avatar)
                )
            }
        }
    }

}