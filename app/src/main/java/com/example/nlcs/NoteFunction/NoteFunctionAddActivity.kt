package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.databinding.ActivityNoteFunctionAcitivityBinding
import com.example.nlcs.databinding.ActivityNoteFunctionAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NoteFunctionAddActivity : AppCompatActivity() {
    private var binding: ActivityNoteFunctionAddBinding? = null

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
        binding = ActivityNoteFunctionAddBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbar?.root)

        binding?.toolbar?.title?.text = "Thêm ghi chú"

        binding?.toolbar?.BackArrow?.setOnClickListener {
            finish() // Return to the previous activity
        }

        // Remove disabling of the save button
        // binding.toolbar.AddMessage.isEnabled = false

        // Remove text watchers for enabling/disabling save button
        // setupTextWatchers()

        binding?.toolbar?.AddMessage?.setOnClickListener {
            val title = binding?.edtTitle?.text.toString().trim()
            val content = binding?.edtContent?.text.toString().trim()

            // Input validation: Check if title and content are not empty
            if (title.isEmpty() || content.isEmpty()) {
                // Show a notification to the user
                Toast.makeText(this, "Tiêu đề và nội dung không được để trống.", Toast.LENGTH_SHORT).show()
            } else {
                val db = FirebaseFirestore.getInstance()
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (currentUserId == null) {
                    Toast.makeText(this, "Bạn phải login để sử dụng chức năng ghi chú.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Create a new message with the user ID
                    val message = Message(
                        messTitle = title,
                        messContent = content,
                        userId = currentUserId
                    )

                    // Add the message to Firestore and get the auto-generated document ID
                    db.collection("notes")
                        .add(message)
                        .addOnSuccessListener { documentReference ->
                            message.messId = documentReference.id // Set the auto-generated ID back to the message object

                            // Return the message with the generated ID to NoteFunctionActivity
                            val intent = Intent().apply {
                                putExtra("Message", message)
                                putExtra(NoteFunctionActivity.KEY, NoteFunctionActivity.TYPE_ADD)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            Toast.makeText(this, "Thêm ghi chú mới thành công!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("FirestoreError", "Error adding document", e)
                            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                        }
                }
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
