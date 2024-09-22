// hiện thông báo khi còn thời gian focus mà thoát
package com.example.nlcs

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.databinding.ActivityTimeDownBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class TimeDown : AppCompatActivity() {

    private var binding : ActivityTimeDownBinding? = null


    // hour
    private val buttonHourPlus: Button by lazy {
        findViewById(R.id.buttonHourPlus)
    }
    private val textViewHour: TextView by lazy {
        findViewById(R.id.textViewHour)
    }
    private val buttonHourMinus: Button by lazy {
        findViewById(R.id.buttonHourMinus)
    }

    // minute
    private val buttonMinutePlus: Button by lazy {
        findViewById(R.id.buttonMinutePlus)
    }
    private val textViewMinute: TextView by lazy {
        findViewById(R.id.textViewMinute)
    }
    private val buttonMinuteMinus: Button by lazy {
        findViewById(R.id.buttonMinuteMinus)
    }

    // second
    private val buttonSecondPlus: Button by lazy {
        findViewById(R.id.buttonSecondPlus)
    }
    private val textViewSecond: TextView by lazy {
        findViewById(R.id.textViewSecond)
    }
    private val buttonSecondMinus: Button by lazy {
        findViewById(R.id.buttonSecondMinus)
    }

    // resume
    private val buttonResume: Button by lazy {
        findViewById(R.id.buttonResume)
    }


    private var hours = 0
    private var minutes = 0
    private var seconds = 0

    private var countDownTimer: CountDownTimer? = null
    private var isCounting = false

    private lateinit var usageTracker: UsageTracker
    private var startTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageTracker = UsageTracker(this)

        binding = ActivityTimeDownBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding?.root)

        hours = intent.getIntExtra("HOURS", 0)
        minutes = intent.getIntExtra("MINUTES", 0)
        seconds = intent.getIntExtra("SECONDS", 0)


        updateUI()

        startCount()

        setSupportActionBar(binding?.toolbarTimeDown)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Quay lại" // Quay lại trang cài đặc đếm giờ
        }
        binding?.toolbarTimeDown?.setNavigationOnClickListener {
            onBackPressed()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // sửa code
        binding?.buttonResume?.setOnClickListener {
            toggleTimer()
        }
        // end tại đây
        buttonResume.setOnClickListener {
            if(hours == 0 && minutes == 0 && seconds == 0){
                return@setOnClickListener
            }
            if(isCounting) {
                stopCount() // pause
            } else {
                startCount() //start
            }
        }


    }
    private fun toggleTimer() {
        if (hours == 0 && minutes == 0 && seconds == 0) return
        if (isCounting) stopCount() else startCount()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateUI() {
        val calendar = Calendar.getInstance()

        calendar.clear()

        // Range hour from 0 to 99
        val hours = this.hours.coerceIn(0, 99)

        calendar.set(Calendar.HOUR_OF_DAY, hours)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, seconds)

        val dateString = SimpleDateFormat("HH:mm:ss").format(calendar.time)

        val dates = dateString.split(":")

        textViewHour.text = if (hours >= 24) String.format("%02d", hours) else dates[0]
        textViewMinute.text = dates[1]
        textViewSecond.text = dates[2]

        // update button text
        if(isCounting) {
            buttonResume.text = "TẠM DỪNG"
        } else {
            buttonResume.text = "TIẾP TỤC" //Start
        }
    }

    private fun updateTimeWithSeconds(totalSeconds: Int){

        hours = totalSeconds / 3600
        minutes = (totalSeconds % 3600) / 60
        seconds = totalSeconds % 60
    }

    private fun startCount() {
        if(countDownTimer != null){
            return
        }

        val totalSecond = seconds + (60 * minutes) + (3600 * hours)

        countDownTimer = object : CountDownTimer(
            (totalSecond.toLong() * 1000), // to milliseconds
            1000,
        ){
            override fun onTick(p0: Long) {
                // update time UI

                updateTimeWithSeconds(
                    (p0 * 0.001).toInt() + 1 // millisecond to second
                    // onTick will be toggle immediately,  so we need plus 1 to make it looks like a real stop watch

                )
                updateUI()
            }

            override fun onFinish() {
                val mediaPlayer = MediaPlayer.create(this@TimeDown, R.raw.alarm) // https://pixabay.com/vi/sound-effects/search/finish/
                mediaPlayer.start()

                // Display completion message
                AlertDialog.Builder(this@TimeDown)
                    .setTitle("Hết thời gian!")
                    .setMessage("Chúc mừng! Bạn đã hoàn thành thời gian tập trung đã đặt ra.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // Stop and release the media player when the dialog is dismissed
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                        }
                        mediaPlayer.release()
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()

                // should reset
                reset()
            }

        }
        countDownTimer?.start()
        isCounting = true
    }

    // Stop count down
    private fun stopCount(){
        countDownTimer?.cancel()
        countDownTimer = null
        isCounting = false

        updateUI()
    }

    // Reset time
    private fun reset(){
        stopCount()

        hours = 0
        minutes = 0
        seconds = 0

        updateUI()
    }

    // Clear all
    override fun onDestroy() {
        stopCount()
        super.onDestroy()
    }

    // Show the remain time when counting down
    override fun onBackPressed() {
        if (isCounting) {
            // Kiểm tra nếu thời gian vẫn còn khi người dùng cố gắng thoát
            val totalSecondsLeft = hours * 3600 + minutes * 60 + seconds
            if (totalSecondsLeft > 0) {
                // Hiển thị hộp thoại cảnh báo nếu còn thời gian
                AlertDialog.Builder(this)
                    .setTitle("Bạn có muốn thoát không?")
                    .setMessage("Bạn vẫn còn $hours giờ, $minutes phút, và $seconds giây. Bạn có muốn thoát không? Thời gian còn lại sẽ không được lưu.")
                    .setPositiveButton("Có") { dialog, _ ->
                        stopCount()
                        dialog.dismiss()
                        super.onBackPressed()
                    }
                    .setNegativeButton("Không") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                super.onBackPressed() // Thực hiện hành động thoát nếu không còn thời gian
            }
        } else {
            super.onBackPressed() // Thực hiện hành động thoát nếu không đếm ngược
        }
    }

// Counting down time and save data into usageTracker
    override fun onResume() {
        super.onResume()

        // tiếp tục đếm thời gian sử dụng function
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()

        // dừng thời gian sử dụng function và lưu vào ussageTracker
        val endTime = System.currentTimeMillis()
        val duration = (endTime - startTime) / 1000 / 60 // Convert to minutes

//        usageTracker.addUsageTime("FocusMode", duration.toInt())

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 phút) thì lưu vào UsageTracker
        if (duration > 0) {
            usageTracker.addUsageTime("Hẹn giờ tập trung", duration.toInt())
        }
        else {
            usageTracker.addUsageTime("Hẹn giờ tập trung", 0)
        }
    }
}