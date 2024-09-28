package com.example.nlcs

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.databinding.ActivityReminderBinding


class ReminderActivity: AppCompatActivity() {
    private var binding: ActivityReminderBinding? = null
    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // import class usageTracker to count using time
        usageTracker = UsageTracker(this)

        enableEdgeToEdge()

        binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set up Toolbar
        setSupportActionBar(binding?.toolbarNote)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Về trang chủ"  // Tùy chỉnh tiêu đề

        // back to main menu
        binding?.toolbarNote?.setNavigationOnClickListener {
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

        // Tính toán thời gian sử dụng Ghi chú
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Nhắc nhở", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Nhắc nhở", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}