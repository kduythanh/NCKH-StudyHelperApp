package com.example.nlcs.ui.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.nlcs.adapter.viewpager.MyViewPagerAdapter
import com.example.nlcs.databinding.FragmentLibraryBinding
import com.example.nlcs.ui.activities.create.CreateFolderActivity
import com.example.nlcs.ui.activities.create.CreateSetActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class LibraryFragment : Fragment() {
    private var binding: FragmentLibraryBinding? = null
    private var currentTabPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupTabLayout()
        setupUserPreferences()
        setupAddButton()
    }

    private fun setupViewPager() {
        val myViewPagerAdapter = MyViewPagerAdapter(
            childFragmentManager,
            FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        binding!!.viewPager.adapter = myViewPagerAdapter
    }

    private fun setupTabLayout() {
        binding!!.tabLayout.setupWithViewPager(binding!!.viewPager)
        binding!!.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTabPosition = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
    }

    private fun setupUserPreferences() {
    }

    private fun setupAddButton() {
        binding!!.addBtn.setOnClickListener { view1: View? ->
            if (currentTabPosition == 0) {
                startActivity(Intent(activity, CreateSetActivity::class.java))
            } else if (currentTabPosition == 1) {
                startActivity(Intent(activity, CreateFolderActivity::class.java))
            }
        }
    }
}