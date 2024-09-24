package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityNoteFunctionAcitivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoteFunctionAcitivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteFunctionAcitivityBinding
    private var arrayItem: ArrayList<Message> = ArrayList() // Full list of messages
    private var filteredList: ArrayList<Message> = ArrayList() // List to store filtered messages
    private var MyAdapter: MyAdapter? = null

    companion object {
        const val KEY = "KEY"
        const val TYPE_EDIT = "TYPE_EDIT"
        const val TYPE_ADD = "TYPE_ADD"
    }

    // Check which activity returns data for handling
    private var startCheckType =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val type = result.data?.extras?.getString(KEY)
                if (type == TYPE_ADD) {
                    val message = result.data?.extras?.get("Message") as? Message
                    if (message != null) {
                        arrayItem.add(0, message)
                        filterMessages("") // Update the displayed list (unfiltered)
                    }
                }
                if (type == TYPE_EDIT) {
                    val message = result.data?.extras?.get("Message") as? Message
                    if (message != null) {
                        for (item in arrayItem) {
                            if (item.messId == message.messId) {
                                item.messTitle = message.messTitle
                                item.messContent = message.messContent
                                break
                            }
                        }
                        filterMessages("") // Update the displayed list (unfiltered)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note_function_acitivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityNoteFunctionAcitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set toolbar
        setSupportActionBar(binding.toolbar.root)
        binding.toolbar.title.text = "Quản lý ghi chú"

        // Set up RecyclerView layout and adapter
        binding.RecycleView.layoutManager = LinearLayoutManager(this)
        binding.RecycleView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        MyAdapter = MyAdapter(this, filteredList) // Bind the adapter to the filtered list
        binding.RecycleView.adapter = MyAdapter

        // Firebase Firestore retrieval
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get current user ID
        db.collection("notes")
            .whereEqualTo("userId", currentUserId) // Filter by user ID
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val message = document.toObject(Message::class.java)
                    arrayItem.add(message) // Add all messages from Firestore to arrayItem
                }
                filterMessages("") // Display all messages initially
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreError", "Error getting documents.", exception)
            }

        // Handle item clicks
        MyAdapter?.onItemClick = { message, position ->
            val intent = Intent(this, NoteFunctionAdjustActivity::class.java)
            intent.putExtra("Message", message)
            startCheckType.launch(intent)
        }

        // Handle AddMessage button click
        binding.toolbar.AddMessage.setOnClickListener {
            val intent = Intent(this, NoteFunctionAddActivity::class.java)
            startCheckType.launch(intent)
        }

        // Add search functionality to the EditText field
        binding.edtFind.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMessages(s.toString()) // Filter messages when the user types
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Filter messages based on the search query
    private fun filterMessages(query: String) {
        val searchQuery = query.lowercase()
        filteredList.clear()

        if (searchQuery.isEmpty()) {
            // If the search query is empty, display all notes
            filteredList.addAll(arrayItem)
        } else {
            // Filter messages by title
            for (message in arrayItem) {
                if (message.messTitle.lowercase().contains(searchQuery)) {
                    filteredList.add(message)
                }
            }
        }

        // Notify the adapter that the dataset has changed
        MyAdapter?.notifyDataSetChanged()
    }
}
