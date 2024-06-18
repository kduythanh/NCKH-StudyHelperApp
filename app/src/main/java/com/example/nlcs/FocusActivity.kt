package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityFocusBinding

class FocusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up hour picker
        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = 23
        binding.hourPicker.value = 0

        // Set up minute picker
        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = 59
        binding.minutePicker.value = 10

        binding.startTimerButton.setOnClickListener {
            val hour = binding.hourPicker.value
            val minute = binding.minutePicker.value
            val durationInMillis = (hour * 60 * 60 *1000) + (minute * 60 * 1000)
            val intent = Intent(this, TimerActivity::class.java).apply{
                putExtra("duration", durationInMillis)
            }
            startActivity(intent)
        }

    }
}