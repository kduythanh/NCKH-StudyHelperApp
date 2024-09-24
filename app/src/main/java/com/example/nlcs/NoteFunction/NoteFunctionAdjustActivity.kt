package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAdjustBinding
import com.google.firebase.firestore.FirebaseFirestore

class NoteFunctionAdjustActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteFunctionAdjustBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note_function_adjust)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
            message?.messTitle = binding.edtTitle.text.toString()
            message?.messContent = binding.edtContent.text.toString()

            if (message != null && message.messId != null) {
                val db = FirebaseFirestore.getInstance()

                // Update the note using its document ID (messId)
                db.collection("notes")
                    .document(message.messId!!) // Use the stored Firestore document ID
                    .set(message) // Update the document in Firestore
                    .addOnSuccessListener {
                        Log.d("Firestore", "Note successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error updating note", e)
                    }
            }

            val intent = Intent().apply {
                putExtra("Message", message)
                putExtra(NoteFunctionAcitivity.KEY, NoteFunctionAcitivity.TYPE_EDIT)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


    }
}

