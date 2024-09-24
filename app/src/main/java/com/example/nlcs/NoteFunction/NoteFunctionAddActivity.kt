package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NoteFunctionAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteFunctionAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note_function_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityNoteFunctionAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)

        binding.toolbar.title.text = "Thêm ghi chú"

        binding.toolbar.BackArrow.setOnClickListener {
            finish() // Return to the previous activity
        }

        binding.toolbar.AddMessage.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

            // Create a new message with the user ID
            val message = Message(
                messTitle = binding.edtTitle.text.toString(),
                messContent = binding.edtContent.text.toString(),
                userId = currentUserId ?: "" // Use a default value if user ID is null
            )

            // Add the message to Firestore and get the auto-generated document ID
            db.collection("notes")
                .add(message) // Let Firebase auto-generate the document ID
                .addOnSuccessListener { documentReference ->
                    message.messId = documentReference.id // Set the auto-generated ID back to the message object

                    // Return the message with the generated ID to NoteFunctionActivity
                    val intent = Intent().apply {
                        putExtra("Message", message)
                        putExtra(NoteFunctionAcitivity.KEY, NoteFunctionAcitivity.TYPE_ADD)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("FirestoreError", "Error adding document", e)
                    Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
