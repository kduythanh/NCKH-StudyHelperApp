package com.example.nlcs

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var editTextCurrentPassword: EditText
    private lateinit var editTextNewPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonChangePassword: Button
    private lateinit var exitButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword)
        editTextNewPassword = findViewById(R.id.editTextNewPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonChangePassword = findViewById(R.id.buttonChangePassword)
        exitButton = findViewById(R.id.exitButton)
        auth = FirebaseAuth.getInstance()

        binding.buttonChangePassword.setOnClickListener {
            Log.d("ChangePassword", "Đã bấm nút Đổi mật khẩu")
            changePassword()
        }
        binding.exitButton.setOnClickListener {
            finish()
        }
    }

    private fun changePassword() {
        val currentPassword = binding.editTextCurrentPassword.text.toString().trim()
        val newPassword = binding.editTextNewPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới.", Toast.LENGTH_SHORT).show()
            return
        }
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu mới.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Mật khẩu mới và xác nhận không khớp.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tiến hành đổi mật khẩu
        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show()
            return
        }
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                            finish() // Kết thúc hoạt động hiện tại
                        } else {
                            Toast.makeText(this, "Đổi mật khẩu thất bại: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("ChangePassword", "Xác thực không thành công: ${task.exception?.message}")
                    Toast.makeText(this, "Xác thực mật khẩu hiện tại không thành công: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}