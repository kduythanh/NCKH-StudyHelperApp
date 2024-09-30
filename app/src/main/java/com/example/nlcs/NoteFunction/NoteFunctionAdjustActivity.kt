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
import com.example.nlcs.databinding.ActivityNoteFunctionAdjustBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoteFunctionAdjustActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteFunctionAdjustBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prevent dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Inflate the layout using view binding
        binding = ActivityNoteFunctionAdjustBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)

        binding.toolbar.title.text = "Chỉnh sửa nội dung ghi chú"
        binding.toolbar.AddMessage.text = "Sửa"

        val message = intent.extras?.get("Message") as? Message
        if (message != null) {
            binding.edtTitle.setText(message.messTitle)
            binding.edtContent.setText(message.messContent)
        }

        binding.toolbar.BackArrow.setOnClickListener {
            finish()
        }

        binding.toolbar.AddMessage.setOnClickListener {
            if (message != null) {
                val title = binding.edtTitle.text.toString().trim()
                val content = binding.edtContent.text.toString().trim()

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
}


