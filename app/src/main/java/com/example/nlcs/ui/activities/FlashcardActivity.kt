package com.example.nlcs.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI

import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.databinding.ActivityFlashcardBinding

import com.example.nlcs.preferen.UserSharePreferences

class FlashcardActivity : AppCompatActivity() {
    private var navController: NavController? = null
    private var binding: ActivityFlashcardBinding? = null
    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageTracker = UsageTracker(this)
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        setupNavigation()
    }

    private fun setupNavigation() {

        // Initialize the navController
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)

        // Define the navGraphId and menuId
        val navGraphId = R.navigation.main_nav
        val menuId = R.menu.menu_nav

        // Set the navigation graph
        navController.setGraph(navGraphId)

        // Clear and inflate the bottom navigation menu
        binding?.bottomNavigationView?.menu?.clear()
        binding?.bottomNavigationView?.inflateMenu(menuId)

        // Set up the navigation controller with the bottom navigation view
        binding?.let { NavigationUI.setupWithNavController(it.bottomNavigationView, navController) }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }
    override fun onResume() {
        super.onResume()
        // Lưu thời gian bắt đầu (mốc thời gian hiện tại) để tính thời gian sử dụng khi Activity bị tạm dừng
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()

        // Tính toán thời gian sử dụng Sơ đồ tư duy
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Thẻ ghi nhớ", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Thẻ ghi nhớ", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Đặt binding thành null an toàn khi Activity bị hủy
        binding = null
    }
}
