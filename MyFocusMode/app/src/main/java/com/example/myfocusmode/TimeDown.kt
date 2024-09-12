// hiện thông báo khi còn thời gian fccus mà thoát
package com.example.myfocusmode

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
import com.example.myfocusmode.databinding.ActivityTimeDownBinding
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

    // thử sửa code

    // end tại đây

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeDownBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        hours = intent.getIntExtra("HOURS", 0)
        minutes = intent.getIntExtra("MINUTES", 0)
        seconds = intent.getIntExtra("SECONDS", 0)


        updateUI()

        startCount()

        setSupportActionBar(binding?.toolbarTimeDown)
        // sửa code
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // end tại đây

        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

//        buttonReset.setOnClickListener {
//            reset() // reset
//        }

    }
    // Sửa code
    private fun toggleTimer() {
        if (hours == 0 && minutes == 0 && seconds == 0) return
        if (isCounting) stopCount() else startCount()
    }

    // end tại đây
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
            buttonResume.text = "PAUSE"
        } else {
            buttonResume.text = "RESUME" //Start
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
                    .setTitle("Time's Up!")
                    .setMessage("Congratulations! You have completed your focus time.")
                    .setPositiveButton("OK") { dialog, _ ->
                        // Stop and release the media player when the dialog is dismissed
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                        }
                        mediaPlayer.release()
                        dialog.dismiss()
                    }
                    .setCancelable(false) // sửa code tại đây nèeeeeee
                    .show()

                // should reset
                reset()
            }

        }
        countDownTimer?.start()
        isCounting = true
    }

    private fun stopCount(){
        countDownTimer?.cancel()
        countDownTimer = null
        isCounting = false

        updateUI()
    }

    private fun reset(){
        stopCount()

        hours = 0
        minutes = 0
        seconds = 0

        updateUI()
    }

    override fun onDestroy() {
        stopCount()
        super.onDestroy()
    }

    // Override onBackPressed to show confirmation dialog
    override fun onBackPressed() {
        if (isCounting) {
            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Exit Timer")
                .setMessage("Are you sure you want to exit? The timer will be stopped. You'll get fail this focus time.")
                .setPositiveButton("Yes") { dialog, _ ->
                    stopCount()
                    dialog.dismiss()
                    super.onBackPressed()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
}