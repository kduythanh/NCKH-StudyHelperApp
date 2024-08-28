package com.example.nlcs

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nlcs.databinding.ActivityMindMapBinding
import com.google.firebase.firestore.FirebaseFirestore

class MindMapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMindMapBinding
    private lateinit var rootNode: TreeNode<String>
    private var currentContextMenu: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable the back arrow
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get the title from intent and set it as the support action bar title
        val mindMapTitle = intent.getStringExtra("mindMapTitle")
        if (mindMapTitle != null) { supportActionBar?.title = mindMapTitle }

        // Get the document ID from intent
        val documentID = intent.getStringExtra("documentID") ?: ""

        // Ends the activity when clicked back arrow functionality
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Line 92
        fetchRootNode(documentID)

        // Line 114
        setupOutsideTouchListener()

        // Hide context menu when zooming or panning gesture is detected
        binding.zoomableView.onGestureListener = {
            currentContextMenu?.visibility = View.GONE
            currentContextMenu = null
        }

        // Listener for the "Done" button
        binding.doneButton.setOnClickListener {
            // Line 73
            saveRootNodeChanges(documentID)
        }

        // Set up the long click listener for the edit text / rootNode
        binding.mainNode.setOnLongClickListener {
            // Line 134
            showContextMenu(binding.mainNode)
            true
        }
    }

    // Save changes method through the Done button
    private fun saveRootNodeChanges(documentID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("mindMapTemp").document(documentID)

        // Convert the root node and its children to a map structure
        val rootNodeMap = convertNodeToMap(rootNode)

        // Update only the rootNode and children fields in Firestore
        val updates = hashMapOf<String, Any>(
            "rootNode" to rootNodeMap
        )

        docRef
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Saved changes", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Failed to save changes: ${e.message}")
            }
    }

    // Get mind map data from Firestore
    private fun fetchRootNode(documentID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("mindMapTemp").document(documentID)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val masterNodeMap = document.get("rootNode") as? Map<*, *>
                    if (masterNodeMap != null) {
                        // Convert the map structure back into a TreeNode
                        rootNode = convertMapToNode(masterNodeMap)
                        binding.mainNode.setText(rootNode.data)

                        // Add the root node and its children to the view
                        addChildNodeToView(rootNode)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Failed to fetch document: ${e.message}")
            }
    }

    private fun convertNodeToMap(node: TreeNode<String>): Map<String, Any> {
        val nodeMap = HashMap<String, Any>()
        nodeMap["data"] = node.data ?: ""

        // Convert children recursively
        val childrenList = node.children.map { convertNodeToMap(it) }
        nodeMap["children"] = childrenList

        return nodeMap
    }

    private fun convertMapToNode(nodeMap: Map<*, *>): TreeNode<String> {
        val data = nodeMap["data"] as? String ?: ""
        val node = TreeNode(data)

        // Convert children recursively
        val childrenList = nodeMap["children"] as? List<Map<*, *>> ?: emptyList()
        for (childMap in childrenList) {
            val childNode = convertMapToNode(childMap)
            node.addChild(childNode)
        }

        return node
    }


    private fun addChildNodeToView(node: TreeNode<String>, parentView: ViewGroup? = null) {
        // Inflate the view for the current node
        val childView = layoutInflater.inflate(R.layout.mind_map_child_node, binding.mindMapContent, false)

        val childEditText = childView.findViewById<EditText>(R.id.childTextBox)
        (childEditText.parent as? ViewGroup)?.removeView(childEditText)
        childEditText.setText(node.data)

        // Positioning logic as needed
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 0) // Modify this to position your nodes correctly
        childEditText.layoutParams = layoutParams

        // Add the current node view to the parent or the root view
        if (parentView != null) {
            parentView.addView(childEditText)
        } else {
            binding.mindMapContent.addView(childEditText)
        }

        // Recursively add all children of the current node
        node.children.forEach { childNode ->
            addChildNodeToView(childNode, binding.mindMapContent)
        }
    }

    // Set up the outside touch listener to hide the context menu when clicked outside
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
                    currentContextMenu?.visibility = View.GONE
                    currentContextMenu = null
                }
            }
            false
        }
    }

    // Show context menu method
    private fun showContextMenu(editTextBox: EditText?) {
        val contextMenuView = layoutInflater.inflate(R.layout.mind_map_context_menu, binding.root, false)

        // Add child button click listeners
        contextMenuView.findViewById<ImageView>(R.id.addChildIcon).setOnClickListener {
                addChildNode(rootNode)
        }

        contextMenuView.findViewById<ImageView>(R.id.deleteChildIcon).setOnClickListener {
            // Implement the logic to delete a child node here
            Toast.makeText(this, "Delete child clicked", Toast.LENGTH_SHORT).show()
        }

        // Copy Text button click listener
        contextMenuView.findViewById<ImageView>(R.id.copyTextIcon).setOnClickListener {
            val textToCopy = editTextBox?.text.toString()
            if (textToCopy.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show()
            }
        }

        // Position the context menu above the text box
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        // Calculate the position of the context menu based on the position of the text box
        val location = IntArray(2)
        editTextBox?.getLocationInWindow(location)
        layoutParams.setMargins(location[0] - 30, location[1] - 150, 0, 0)

        // Add the context menu to the root view
        binding.root.addView(contextMenuView, layoutParams)

        // Show the context menu
        contextMenuView.visibility = View.VISIBLE
        currentContextMenu = contextMenuView
    }

    private fun addChildNode(parentNode: TreeNode<String>) {
        val childNode = TreeNode("New Child Node")
        parentNode.addChild(childNode)

        val childView = layoutInflater.inflate(R.layout.mind_map_child_node, binding.mindMapContent, false)
        val childEditText = childView.findViewById<EditText>(R.id.childTextBox)
        (childEditText.parent as? ViewGroup)?.removeView(childEditText)
        childEditText.setText(childNode.data)

        binding.mindMapContent.addView(childEditText)

        childEditText.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        val parentLocation = IntArray(2)
        binding.mainNode.getLocationInWindow(parentLocation)

        val layoutParams = childEditText.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(parentLocation[0] + binding.mainNode.width - 550, parentLocation[1] - 400, 0, 0)
    }
}
