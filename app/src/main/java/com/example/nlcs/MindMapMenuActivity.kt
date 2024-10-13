package com.example.nlcs

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.databinding.ActivityMindMapMenuBinding

class MindMapMenuActivity : AppCompatActivity() {

    private var binding: ActivityMindMapMenuBinding ?= null
    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // import class usageTracker to count using time
        usageTracker = UsageTracker(this)

        enableEdgeToEdge()

        binding = ActivityMindMapMenuBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set up Toolbar
        setSupportActionBar(binding?.toolbarMindMap)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Về trang chủ"  // Tùy chỉnh tiêu đề

        // back to main menu
        binding?.toolbarMindMap?.setNavigationOnClickListener {
            onBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


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
            usageTracker.addUsageTime("Sơ đồ tư duy", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Sơ đồ tư duy", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}