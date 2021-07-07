package com.ameow.silbo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ameow.silbo.service.MsgService
import com.ameow.silbo.ui.login.LoginActivity
import com.ameow.silbo.ui.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private val KEY_POSITION = "keyPosition"
    private var navPosition: BottomNavigationPosition = BottomNavigationPosition.CHATLIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSaveInstanceState(savedInstanceState)

        // 检查是否已经登录，如未登录启动LoginActivity
        val prefs = getSharedPreferences("runtime", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)?:startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )

        setContentView(R.layout.activity_main)

        findViewById<BottomNavigationView>(R.id.nav_view).apply {
            val bottomNavigation: BottomNavigationView = findViewById(R.id.nav_view)
            if (Build.VERSION.SDK_INT <= 27) {
                bottomNavigation.disableShiftMode()
            }
            active(navPosition.position)
            setOnNavigationItemSelectedListener { item ->
                navPosition = findNavigationPositionById(item.itemId)
                switchFragment(navPosition)
            }
        }
        initFragment(savedInstanceState)

//        // 启动MsgService
//        Log.d("MsgService", "starting msgService")
//        val msgServiceIntent = Intent(this, MsgService::class.java)
//        startService(msgServiceIntent)
    }

    private fun restoreSaveInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.getInt(KEY_POSITION, BottomNavigationPosition.CHATLIST.id)?.also {
            navPosition = findNavigationPositionById(it)
        }
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        savedInstanceState ?: switchFragment(BottomNavigationPosition.CHATLIST)
    }

    private fun switchFragment(navPosition: BottomNavigationPosition): Boolean {
        return findFragment(navPosition).let {
            if (it.isAdded) return false
            supportFragmentManager.detach()
            supportFragmentManager.attach(it, navPosition.getTag())
            supportFragmentManager.executePendingTransactions()
        }
    }

    private fun findFragment(position: BottomNavigationPosition): Fragment {
        return supportFragmentManager.findFragmentByTag(position.getTag()) ?: position.createFragment()
    }

    override fun onResume() {
        super.onResume()
        // 检查是否已经登录，如未登录启动LoginActivity
        val prefs = getSharedPreferences("runtime", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", null)?:startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止msgService
        val msgServiceIntent = Intent(this, MsgService::class.java)
        stopService(msgServiceIntent)
    }

}