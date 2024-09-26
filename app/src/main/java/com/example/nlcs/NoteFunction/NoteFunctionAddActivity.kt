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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAddBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NoteFunctionAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteFunctionAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //Prevent dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
            //Return to the previous activity
//            val intent = Intent(this,NoteFunctionAcitivity::class.java)
//            startActivity(intent)
            finish()
        }

//        val db = FirebaseFirestore.getInstance()
//        db.collection()

//        binding.btnSelectImage.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(intent, REQUEST_IMAGE_PICK)
//        }

        binding.toolbar.AddMessage.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            // Create a new message without an ID initially
            val message = Message(
                messTitle = binding.edtTitle.text.toString(),
                messContent = binding.edtContent.text.toString()
            )

            // Add the message to Firestore and get the auto-generated document ID
            db.collection("notes")
                .add(message) // Let Firebase auto-generate the document ID
                .addOnSuccessListener { documentReference ->
                    message.messId =
                        documentReference.id // Set the auto-generated ID back to the message object

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
                }
        }

    }

}