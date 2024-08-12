package com.example.nlcs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityMindMapBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class MindMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMindMapBinding
    private lateinit var rootNode: Node
    private var currentContextMenu: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable the back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize the root node
        @Suppress("DEPRECATION")
        rootNode = intent.getSerializableExtra("rootNode")
                as Node? ?: Node(
            id = UUID.randomUUID().toString(),
            text = "Main Idea",
            children = listOf()
        )

        // Initialize the edit text
        val editTextBox = findViewById<EditText>(R.id.mainNode)
        // Get the document ID from the intent
        val documentId = intent.getStringExtra("documentID")

        // Insert the title of the mind map into the toolbar
        val mindMapTitle = intent.getStringExtra("mindMapTitle")
        if (mindMapTitle != null) {
            //Set the title of the toolbar to the mind map title
            supportActionBar?.title = mindMapTitle
        }


        // Ends the activity when clicked back arrow functionality
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // fetch the (main node for now) node from Firestore
        if(documentId != null){
            fetchNodeFromFirestore(documentId, editTextBox)
        }

        // Set the behavior of the done button
        binding.doneButton.setOnClickListener {
            rootNode.text = editTextBox.text.toString()
            if (documentId != null) {
                saveNodeToFirebase(documentId, rootNode)
            }
            finish()
        }

        // Set up the outside touch listener
        setupOutsideTouchListener()
        // Hide context menu when panning or zooming
        binding.zoomableView.onGestureListener = {
            hideContextMenu()
        }

        // Set up the long click listener for the edit text
        editTextBox.setOnLongClickListener{
            showContextMenu(editTextBox)
            true
        }
    }

    // Hide the context menu method
    private fun hideContextMenu() {
        currentContextMenu?.visibility = View.GONE
        currentContextMenu = null
    }

    // Set up the outside touch listener method
    @SuppressLint("ClickableViewAccessibility")
    private fun setupOutsideTouchListener() {
        binding.root.setOnTouchListener { _, event ->
            if (currentContextMenu != null && event.action == MotionEvent.ACTION_DOWN) {
                val location = IntArray(2)
                currentContextMenu?.getLocationOnScreen(location)

                val x = event.rawX
                val y = event.rawY

                if (x < location[0] || x > location[0] + currentContextMenu!!.width ||
                    y < location[1] || y > location[1] + currentContextMenu!!.height) {
                    hideContextMenu()
                }
            }
            false
        }
    }

    // Fetch the node from Firestore method
    private fun fetchNodeFromFirestore(documentId: String, editTextBox: EditText?) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("mindMapTemp").document(documentId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Retrieves the MasterNode field from the Firestore document
                    // MasterNode is expected to be a map where the keys and values can be of any type (* stands for any type).
                    val masterNode = document.get("MasterNode") as Map<*, *>
                    // Extract the text Field and tries to cast the value to a String
                    // If the cast to String returns null, this part assigns the default value "Main Idea" to the text variable.
                    val text = masterNode["text"] as? String ?: "Main Idea"

                    // There will be codes for the children later

                    // Set the retrieved text to the EditText
                    editTextBox?.setText(text)
                } else {
                    Toast.makeText(this, "Document not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save the node to Firestore method through the done button
    private fun saveNodeToFirebase( documentId: String, rootNode: Node) {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("mindMapTemp").document(documentId)

        // Convert the node to a map or another suitable format
        val nodeData = hashMapOf(
            "MasterNode.text" to rootNode.text,
//            "children" to rootNode.children.map { it.id } // Not implemented yet
        )

        // Update the existing document instead of creating a new one
        docRef.update(nodeData as Map<String, Any?>)
            .addOnSuccessListener {
                Toast.makeText(this, "Node updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update node: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Show context menu method
    private fun showContextMenu(editTextBox: EditText?) {
        val contextMenuView = layoutInflater.inflate(R.layout.mind_map_context_menu, binding.root, false)
        // Set up the click listeners for the context menu icons
        contextMenuView.findViewById<ImageView>(R.id.addChildIcon).setOnClickListener {
            Toast.makeText(this, "Add child clicked", Toast.LENGTH_SHORT).show()
        }
        contextMenuView.findViewById<ImageView>(R.id.deleteChildIcon).setOnClickListener {
            Toast.makeText(this, "Delete child clicked", Toast.LENGTH_SHORT).show()
        }
        contextMenuView.findViewById<ImageView>(R.id.copyTextIcon).setOnClickListener {
            Toast.makeText(this, "Copy text clicked", Toast.LENGTH_SHORT).show()
        }

        // Position the context menu above the text box
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        // Calculate the position of the context menu
        // based on the position of the text box
        val location = IntArray(2)
        editTextBox?.getLocationInWindow(location)
        layoutParams.setMargins(location[0] - 30, location[1] - 150, 0, 0)

        // Add the context menu to the root view
        binding.root.addView(contextMenuView, layoutParams)

        // Show the context menu
        contextMenuView.visibility = View.VISIBLE
        currentContextMenu = contextMenuView

    }

}