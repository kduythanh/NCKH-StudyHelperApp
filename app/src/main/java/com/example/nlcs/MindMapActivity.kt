//package com.example.nlcs
//
//import android.os.Bundle
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.EditorInfo
//import android.view.inputmethod.InputMethodManager
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.RelativeLayout
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.isVisible
//import com.example.nlcs.databinding.ActivityMindMapBinding
//import java.util.UUID
//
//class MindMapActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMindMapBinding
//    private lateinit var rootNode: Node
////    private lateinit var contentLayout: RelativeLayout
//    private var currentContextMenu: View? = null
//    private var isContextMenuOpen = false
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMindMapBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Enable the back arrow
//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//        // Initialize the edit text
//        val editTextBox = findViewById<EditText>(R.id.textBox)
//
////        val zoomableView = findViewById<ZoomableView>(R.id.zoomableView)
////        contentLayout = findViewById(R.id.mindMapContent)
//
//        // Insert the title of the mind map into the toolbar
//        val mindMapTitle = intent.getStringExtra("mindMapTitle")
//        if (mindMapTitle != null) {
//            //Set the title of the toolbar to the mind map title
//            supportActionBar?.title = mindMapTitle
//        }
//
//        // Get the root node from the intent
//        @Suppress("DEPRECATION")
//        rootNode = intent.getSerializableExtra("rootNode")
//                as Node? ?: Node(
//            id = UUID.randomUUID().toString(),
//            text = "Main Idea2",
//            children = listOf()
//        )
//
//        // Ends the activity when clicked back arrow functionality
//        binding.toolbar.setNavigationOnClickListener {
//            finish()
//        }
//
//        // Set initial text to the EditText
//        editTextBox.setText(rootNode.text)
//
//        editTextBox.setOnClickListener {
//
//        }
//    }
//}