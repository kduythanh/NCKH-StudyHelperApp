package com.example.nlcs

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.HandlerCompat
import com.example.nlcs.databinding.ActivitySplashBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.widget.Button
import android.widget.TextView


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase
        firebaseAuth = Firebase.auth

        // Create an async Handler for the main Looper
        val handler = HandlerCompat.createAsync(mainLooper)

        // Delay for 1 second and move the app to the next activity depend on login status
        handler.postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if(currentUser == null){
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 800)
    }

}

