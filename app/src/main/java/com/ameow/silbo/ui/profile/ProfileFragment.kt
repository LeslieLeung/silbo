package com.ameow.silbo.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ameow.silbo.R
import com.ameow.silbo.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    companion object {
        val TAG: String = ProfileFragment::class.java.simpleName
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ViewModel

        val btnExit: Button = requireActivity().findViewById(R.id.logout)
        btnExit.setOnClickListener {
            val editor = requireActivity().getSharedPreferences("runtime", Context.MODE_PRIVATE).edit()
            // 清空登录信息
            editor.putString("user_id", null)
            editor.putString("username", null)
            editor.apply()
            startActivity(Intent(context, LoginActivity::class.java))
        }

        val prefs = requireActivity().getSharedPreferences("runtime", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", "")

        val profileUsername: TextView = requireActivity().findViewById(R.id.profileUsername)
        profileUsername.text = "用户id：${userId}"

        val avatar: ImageView = requireView().findViewById(R.id.avatar)
        avatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, 2)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            2 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let {
                        uri ->
                        val bitmap = getBitmapFromUri(uri)
                        val avatar: ImageView = requireView().findViewById(R.id.avatar)
                        avatar.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri) = requireActivity().contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }


}