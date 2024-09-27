package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.nlcs.databinding.ActivityForgotPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        // Click listeners for reset button
        binding.forgotPasswordResetButton.setOnClickListener {
            forgotPassword()
        }

        // Click listener for back button
        binding.forgotPasswordBackButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Forgot password function
    private fun forgotPassword() {
        val mail = binding.forgotPasswordEmailEditText.text.toString()
        val isValid = validateEmail(mail)

        if(!isValid) return

        // Send password reset email to user's email
        firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()
            } else{
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // validate email
    private fun validateEmail(mail: String): Boolean {
        if (mail.isEmpty()) {
            binding.forgotPasswordEmailEditText.error = "Please enter your email"
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            binding.forgotPasswordEmailEditText.error = "Invalid email format"
            return false
        }
        return true
    }
}