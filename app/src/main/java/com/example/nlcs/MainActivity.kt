package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        // Test Click listener for log out button
        binding.logOutTestButotn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}