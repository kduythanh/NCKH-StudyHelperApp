package com.example.nlcs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import com.example.nlcs.databinding.ActivitySplashBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        val handler = HandlerCompat.createAsync(mainLooper)

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
        }, 1000)
    }
}