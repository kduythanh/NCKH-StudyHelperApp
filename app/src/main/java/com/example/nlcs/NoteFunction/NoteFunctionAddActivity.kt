package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAddBinding
import com.google.firebase.firestore.FirebaseFirestore

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

        binding.toolbar.title.text = "Thêm tin nhắn"

        binding.toolbar.BackArrow.setOnClickListener{
            //Return to the previous activity
//            val intent = Intent(this,NoteFunctionAcitivity::class.java)
//            startActivity(intent)
            finish()
        }

//        val db = FirebaseFirestore.getInstance()
//        db.collection()

        binding.toolbar.AddMessage.setOnClickListener {
            val message = Message(
                messId = System.currentTimeMillis(),
                messTitle = binding.edtTitle.text.toString(),
                messContent = binding.edtContent.text.toString()
            )
            val intent = Intent().apply {
                putExtra("Message", message)
                putExtra(NoteFunctionAcitivity.KEY, NoteFunctionAcitivity.TYPE_ADD)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


    }


}