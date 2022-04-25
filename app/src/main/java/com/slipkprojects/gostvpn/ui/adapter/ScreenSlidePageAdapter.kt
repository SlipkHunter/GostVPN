package com.slipkprojects.gostvpn.ui.adapter

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class ScreenSlidePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    private val res: Resources = fm.resources
    internal class Tab(var fragmentClass: Class<out Fragment>, var mName: String)

    private val mTabs = Vector<Tab>()

    fun addTab(@StringRes name: Int, fragmentClass: Class<out Fragment>) {
        mTabs.add(Tab(fragmentClass, res.getString(name)))
    }

    fun getTitleByPosition(position: Int): String = mTabs[position].mName

    override fun getItemCount(): Int = mTabs.size

    override fun createFragment(position: Int): Fragment {
        return mTabs[position].fragmentClass.newInstance()
    }
}
