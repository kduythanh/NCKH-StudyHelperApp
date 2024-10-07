package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityLogInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
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
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this, "Email chưa được xác nhận, vui lòng vào hộp thư để kiểm tra lại!", Toast.LENGTH_SHORT).show()
                }
            }else{
                // Xử lý thông báo lỗi cho tài khoản không tồn tại và mật khẩu không chính xác
                val errorMessage = when (task.exception) {
                    is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại, vui lòng nhập lại!"
                    is FirebaseAuthInvalidCredentialsException -> "Tài khoản hoặc mật khẩu không chính xác, vui lòng nhập lại!"
                    else -> task.exception?.localizedMessage ?: "Đã xảy ra lỗi, vui lòng thử lại!"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                // Ghi log thông tin chi tiết
                Log.e("LoginError", "Error: ${task.exception?.message}")
            }
        }
    }

    // Validate account
    private fun validateAccount(mail: String, passwd: String): Boolean{
        if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            binding.LogInEmailEditText.error = "Định dạng email không hợp lệ, vui lòng nhập lại!"
            return false
        }

        if(passwd.length < 6){
            binding.LogInPasswordEditText.error = "Mật khẩu phải có ít nhất 6 ký tự, vui lòng nhập lại!"
            return false
        }
        return true
    }
}