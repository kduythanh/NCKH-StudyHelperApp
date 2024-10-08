package com.example.nlcs.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.nlcs.ui.fragments.library.FoldersFragment
import com.example.nlcs.ui.fragments.library.StudySetsFragment

class MyViewPagerAdapter(fm: FragmentManager, behavior: Int) :
    FragmentStatePagerAdapter(fm, behavior) {
    override fun getItem(position: Int): Fragment {
        return if (position == 1) {
            FoldersFragment()
        } else {
            StudySetsFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Study sets"
            1 -> "Folders"
            else -> ""
        }
    }
}