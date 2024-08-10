package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAdjustBinding

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

        binding.toolbar.title.text = "Chỉnh sửa nội dung"

        binding.toolbar.AddMessage.text = "Sửa"

        val message = intent.extras?.get("Message") as? Message
        if (message != null) {
            binding.edtTitle.setText(message.messTitle)
            binding.edtContent.setText(message.messContent)
        }

        binding.toolbar.BackArrow.setOnClickListener{
            finish()
        }

        binding.toolbar.AddMessage.setOnClickListener {
            message?.messTitle = binding.edtTitle.text.toString()
            message?.messContent = binding.edtContent.text.toString()
            val intent = Intent().apply {
                putExtra("Message", message)
                putExtra(NoteFunctionAcitivity.KEY, NoteFunctionAcitivity.TYPE_EDIT)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}