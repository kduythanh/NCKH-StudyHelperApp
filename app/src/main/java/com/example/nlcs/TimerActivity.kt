package com.example.nlcs

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityTimerBinding


class TimerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}