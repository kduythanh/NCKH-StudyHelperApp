package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        firebaseAuth = Firebase.auth

        // Click listener for sign up button
        binding.signUpCreateAccountButton.setOnClickListener{
            createAccount()
        }

        // Click listener for sign in text view
        binding.SignInLink.setOnClickListener{
            val intent = Intent(this,LogInActivity::class.java)
            startActivity(intent)
        }
    }

    // Create account
    private fun createAccount() {
        val mail = binding.signUpEmailEditText.text.toString()
        val passwd = binding.signUpPasswordEditText.text.toString()
        val confirmPasswd = binding.signUpConfirmPasswordEditText.text.toString()
        val isValid = validateAccount(mail, passwd, confirmPasswd)

        if(!isValid) return

        createAccountFirebase(mail, passwd)
    }

    // Create account in Firebase
    private fun createAccountFirebase(mail: String, passwd: String){
        firebaseAuth.createUserWithEmailAndPassword(mail, passwd).addOnCompleteListener(this){ task ->
            if(task.isSuccessful){
                Toast.makeText(this, "Tài khoản đã được tạo thành công!", Toast.LENGTH_SHORT).show()
                // Send verification email
                firebaseAuth.currentUser?.sendEmailVerification()
                firebaseAuth.signOut()
                finish()
            }else{
                Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validate account
    private fun validateAccount(mail: String, passwd: String, confirmPasswd: String): Boolean{

        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            binding.signUpEmailEditText.error = "Định dạng email không đúng!"
            return false
        }

        if(passwd.length < 6){
            binding.signUpPasswordEditText.error = "Mật khẩu phải có ít nhất 6 ký tự!"
            return false
        }

        if(passwd != confirmPasswd){
            binding.signUpConfirmPasswordEditText.error = "Mật khẩu không khớp với mật khẩu đã nhập ở trên!"
            return false
        }
        return true
    }
}