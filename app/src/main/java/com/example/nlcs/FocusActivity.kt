package com.example.nlcs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.databinding.ActivityFocusBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class FocusActivity : AppCompatActivity() {

    private var binding:ActivityFocusBinding? = null

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0


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

    // reset
    private val buttonReset: Button by lazy {
        findViewById(R.id.buttonReset)
    }

    private var hours = 0
    private var minutes = 0
    private var seconds = 0

    // EditText for input
    private lateinit var editTextHour: EditText
    private lateinit var editTextMinute: EditText
    private lateinit var editTextSecond: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // import class usageTracker to count using time
        usageTracker = UsageTracker(this)

        binding = ActivityFocusBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarFocusMode)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Về trang chủ"  // Trở về trang chủ
        }
        binding?.toolbarFocusMode?.setNavigationOnClickListener {
            onBackPressed()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        editTextHour = findViewById(R.id.textViewHour)
        editTextMinute = findViewById(R.id.textViewMinute)
        editTextSecond = findViewById(R.id.textViewSecond)


        // Set OnTouchListener for EditText
        setupEditTextFocus(editTextHour)
        setupEditTextFocus(editTextMinute)
        setupEditTextFocus(editTextSecond)

        // Set OnEditorActionListener to handle Enter key press
        setupEditorActionListener(editTextHour)
        setupEditorActionListener(editTextMinute)
        setupEditorActionListener(editTextSecond)

        // Set listener on start button
        binding?.flStart?.setOnClickListener {
            try {
                saveInput()
                val intent = Intent(this, TimeDown::class.java)
                intent.putExtra("HOURS", hours)
                intent.putExtra("MINUTES", minutes)
                intent.putExtra("SECONDS", seconds)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("Hẹn giờ tập trung", "Lỗi khi chuyển sang giao diện đếm ngụợc thời gian", e)
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        buttonHourPlus.setOnClickListener {
            hours = plusTime(editTextHour)
            updateUI()
        }
        buttonHourMinus.setOnClickListener {
            hours = minusTime(editTextHour)
            updateUI()
        }

        buttonMinutePlus.setOnClickListener {
            minutes = plusTime(editTextMinute)
            updateUI()
        }
        buttonMinuteMinus.setOnClickListener {
            minutes = minusTime(editTextMinute)
            updateUI()
        }

        buttonSecondPlus.setOnClickListener {
            seconds = plusTime(editTextSecond)
            updateUI()
        }
        buttonSecondMinus.setOnClickListener {
            seconds = minusTime(editTextSecond) // seconds
            updateUI()
        }


        buttonReset.setOnClickListener {
            reset() // reset
        }
    }


    private fun saveInput() {
        // Get input from EditText
        hours = editTextHour.text.toString().toIntOrNull() ?: 0
        minutes = editTextMinute.text.toString().toIntOrNull() ?: 0
        seconds = editTextSecond.text.toString().toIntOrNull() ?: 0

        // Validate input ranges
        if (hours !in 0..99) {
            Toast.makeText(this, "Giờ phải nằm trong khoảng từ 0 đến 99", Toast.LENGTH_SHORT).show()
            hours = 0
        }
        if (minutes !in 0..59) {
            Toast.makeText(this, "Phút phải nằm trong khoảng từ 0 đến 59", Toast.LENGTH_SHORT).show()
            minutes = 0
        }
        if (seconds !in 0..59) {
            Toast.makeText(this, "Giây phải nằm trong khoảng từ 0 đến 59", Toast.LENGTH_SHORT).show()
            seconds = 0
        }

        updateUI()  // Update the UI with the validated input
    }


    private fun setupEditTextFocus(editText: EditText) {
        editText.setOnClickListener {
            editText.setSelection(0, editText.text.length)
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editText.post {
                    editText.setSelection(0, editText.text.length)
                }
            }
        }
    }


    private fun setupEditorActionListener(editText: EditText) {
        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {

                // Save input when the user presses OK
                saveInput()

                // Hide keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                // Clear focus if needed
                v.clearFocus()
                true
            } else {
                false
            }
        }
    }

    // plusTime

    private fun plusTime(editText: EditText): Int {
        val currentTime = editText.text.toString().toIntOrNull() ?: 0
        var updatedTime = currentTime + 1

        when (editText.id) {
            R.id.textViewHour -> {
                // Hours: max is 99
                if (updatedTime > 99) updatedTime = 99
                hours = updatedTime // Update internal state
            }
            R.id.textViewMinute -> {
                // Minutes: max is 59
                if (updatedTime > 59) updatedTime = 59
                minutes = updatedTime // Update internal state
            }
            R.id.textViewSecond -> {
                // Seconds: max is 59
                if (updatedTime > 59) updatedTime = 59
                seconds = updatedTime // Update internal state
            }
        }

        editText.setText(String.format("%02d", updatedTime)) // Update EditText with two-digit formatting
        return updatedTime
    }

    // minusTime

    private fun minusTime(editText: EditText): Int {
        val currentTime = editText.text.toString().toIntOrNull() ?: 0
        var updatedTime = currentTime - 1

        if (updatedTime < 0) updatedTime = 0 // Min value is 0

        when (editText.id) {
            R.id.textViewHour -> hours = updatedTime
            R.id.textViewMinute -> minutes = updatedTime
            R.id.textViewSecond -> seconds = updatedTime
        }

        editText.setText(String.format("%02d", updatedTime)) // Update EditText with two-digit formatting
        return updatedTime
    }

    private fun updateUI() {
        val calendar = Calendar.getInstance()

        calendar.clear()

        // Range hour from 0 to 99
        val hours = this.hours.coerceIn(0, 99)

        calendar.set(Calendar.HOUR_OF_DAY, hours % 24)
        calendar.set(Calendar.MINUTE, minutes)
        calendar.set(Calendar.SECOND, seconds)

        val dateString = SimpleDateFormat("HH:mm:ss").format(calendar.time)

        val dates = dateString.split(":")

        textViewHour.text = if (hours >= 24) String.format("%02d", hours) else dates[0]
        textViewMinute.text = dates[1]
        textViewSecond.text = dates[2]

        // Update EditText fields directly
        editTextHour.setText(String.format("%02d", hours))
        editTextMinute.setText(String.format("%02d", minutes))
        editTextSecond.setText(String.format("%02d", seconds))
    }


    private fun reset(){

        hours = 0
        minutes = 0
        seconds = 0

        updateUI()
    }


// Save using time into statistic
    override fun onResume() {
        super.onResume()

        // Lưu thời gian bắt đầu (mốc thời gian hiện tại) để tính thời gian sử dụng khi Activity bị tạm dừng
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()

        // Tính toán thời gian sử dụng Hẹn giờ tập trung
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Hẹn giờ tập trung", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Hẹn giờ tập trung", 0)
        }
    }
// End saving time to statistic

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}