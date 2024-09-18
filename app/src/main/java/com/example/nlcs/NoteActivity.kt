package com.example.nlcs

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.databinding.ActivityNoteBinding

class NoteActivity : AppCompatActivity() {

    private var binding: ActivityNoteBinding? = null
    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // import class usageTracker to count using time
        usageTracker = UsageTracker(this)

        enableEdgeToEdge()

        binding = ActivityNoteBinding.inflate(layoutInflater)
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

        // Tính toán thời gian sử dụng Note
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInMinutes = (durationInMillis / 1000 / 60).toInt() // Chuyển đổi thời gian từ milliseconds sang phút

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 phút) thì lưu vào UsageTracker
        if (durationInMinutes > 0) {
            usageTracker.addUsageTime("Note", durationInMinutes)
        }
        else {
            usageTracker.addUsageTime("Note", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}