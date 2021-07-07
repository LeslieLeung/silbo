package com.ameow.silbo.ui.navigation

import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import com.ameow.silbo.R
import com.ameow.silbo.ui.chatlist.ChatlistFragment
import com.ameow.silbo.ui.contact.ContactFragment
import com.ameow.silbo.ui.profile.ProfileFragment

enum class BottomNavigationPosition(val position:Int, val id: Int) {
    CHATLIST(0, R.id.tab_chatlist),
    CONTACT(1, R.id.tab_contact),
    PROFILE(2, R.id.tab_profile)
}

fun findNavigationPositionById(id: Int): BottomNavigationPosition = when (id) {
    BottomNavigationPosition.CHATLIST.id -> BottomNavigationPosition.CHATLIST
    BottomNavigationPosition.CONTACT.id -> BottomNavigationPosition.CONTACT
    BottomNavigationPosition.PROFILE.id -> BottomNavigationPosition.PROFILE
    else -> BottomNavigationPosition.CHATLIST
}

fun BottomNavigationPosition.createFragment(): Fragment = when (this) {
    BottomNavigationPosition.CHATLIST -> ChatlistFragment.newInstance()
    BottomNavigationPosition.CONTACT -> ContactFragment.newInstance()
    BottomNavigationPosition.PROFILE -> ProfileFragment.newInstance()
}

fun BottomNavigationPosition.getTag(): String = when (this) {
    BottomNavigationPosition.CHATLIST -> ChatlistFragment.TAG
    BottomNavigationPosition.CONTACT -> ContactFragment.TAG
    BottomNavigationPosition.PROFILE -> ProfileFragment.TAG
}