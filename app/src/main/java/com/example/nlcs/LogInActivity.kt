package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityLogInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        // Click listeners for login button
        binding.LogInButton.setOnClickListener {
            loginUser()
        }

        // Click listener for create account text view
        binding.createAccountLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Click listener for forgot password text view
        binding.forgotPasswordTextView.setOnClickListener{
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // Login user
    private fun loginUser(){
        val mail = binding.LogInEmailEditText.text.toString()
        val passwd = binding.LogInPasswordEditText.text.toString()
        val isValid = validateAccount(mail, passwd)

        if(!isValid) return

        loginAccountInFirebase(mail, passwd)
    }

    // Login account in firebase
    private fun loginAccountInFirebase(mail: String, passwd: String){
        firebaseAuth.signInWithEmailAndPassword(mail, passwd).addOnCompleteListener(this){ task ->
            if(task.isSuccessful){
                if(firebaseAuth.currentUser?.isEmailVerified == true){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Email not verified, please verify your email!", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validate account
    private fun validateAccount(mail: String, passwd: String): Boolean{
        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            binding.LogInEmailEditText.error = "Invalid email format"
            return false
        }

        if(passwd.length < 6){
            binding.LogInPasswordEditText.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }
}