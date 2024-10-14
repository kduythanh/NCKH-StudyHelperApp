package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.databinding.ActivityNoteFunctionAdjustBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoteFunctionAdjustActivity : AppCompatActivity() {
    private var binding: ActivityNoteFunctionAdjustBinding? = null

    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageTracker = UsageTracker(this)
        // Prevent dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Inflate the layout using view binding
        binding = ActivityNoteFunctionAdjustBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbar?.root)

        binding?.toolbar?.title?.text = "Sửa nội dung ghi chú"
        binding?.toolbar?.AddMessage?.text = "Sửa"

        val message = intent.extras?.get("Message") as? Message
        if (message != null) {
            binding?.edtTitle?.setText(message.messTitle)
            binding?.edtContent?.setText(message.messContent)
        }

        binding?.toolbar?.BackArrow?.setOnClickListener {
            finish()
        }

        binding?.toolbar?.AddMessage?.setOnClickListener {
            if (message != null) {
                val title = binding?.edtTitle?.text.toString().trim()
                val content = binding?.edtContent?.text.toString().trim()

                // Input validation: Check if title and content are not empty
                if (title.isEmpty() || content.isEmpty()) {
                    // Show a notification to the user
                    Toast.makeText(this, "Tiêu đề và nội dung không được để trống.", Toast.LENGTH_SHORT).show()
                } else {
                    message.messTitle = title
                    message.messContent = content
                    message.userId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // Update user ID

                    val db = FirebaseFirestore.getInstance()

                    // Update the note using its document ID (messId)
                    db.collection("notes")
                        .document(message.messId!!) // Use the stored Firestore document ID
                        .set(message) // Update the document in Firestore
                        .addOnSuccessListener {
                            Log.d("Firestore", "Note successfully updated!")

                            val intent = Intent().apply {
                                putExtra("Message", message)
                                putExtra(NoteFunctionActivity.KEY, NoteFunctionActivity.TYPE_EDIT)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            Toast.makeText(this, "Cập nhật ghi chú thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating note", e)
                            Toast.makeText(this, "Không thể cập nhật ghi chú.", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Lỗi: Dữ liệu ghi chú bị thiếu.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Lưu thời gian bắt đầu (mốc thời gian hiện tại) để tính thời gian sử dụng khi Activity bị tạm dừng
        startTime = System.currentTimeMillis()

    }


    override fun onPause() {
        super.onPause()

        // Tính toán thời gian sử dụng Ghi chú
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Ghi chú", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Ghi chú", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}


