package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        firebaseAuth = Firebase.auth

        binding.forgotPasswordResetButton.setOnClickListener {
            forgotPassword()
        }

        binding.forgotPasswordBackButton.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun forgotPassword() {
        val mail = binding.forgotPasswordEmailEditText.text.toString()
        val isValid = validateEmail(mail)

        if(!isValid) return

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