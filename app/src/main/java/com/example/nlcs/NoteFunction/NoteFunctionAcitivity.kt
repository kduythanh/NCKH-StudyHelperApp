package com.example.nlcs.NoteFunction

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.UsageTracker
import com.example.nlcs.databinding.ActivityNoteFunctionAcitivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NoteFunctionActivity : AppCompatActivity() {
    private var binding: ActivityNoteFunctionAcitivityBinding ? = null
    private var arrayItem: MutableList<Message> = mutableListOf() // Full list of messages
    private lateinit var myAdapter: MyAdapter // Adapter instance


    // Declare usageTracker to use UsageTracker class
    private lateinit var usageTracker: UsageTracker
    // Setting saving time start at 0
    private var startTime: Long = 0

    companion object {
        const val KEY = "KEY"
        const val TYPE_EDIT = "TYPE_EDIT"
        const val TYPE_ADD = "TYPE_ADD"
    }

    // Check which activity returns data for handling
    private val startCheckType =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val type = result.data?.extras?.getString(KEY)
                val message = result.data?.extras?.get("Message") as? Message
                if (message != null) {
                    when (type) {
                        TYPE_ADD -> {
                            arrayItem.add(0, message) // Add to the beginning of the list
                        }
                        TYPE_EDIT -> {
                            val index = arrayItem.indexOfFirst { it.messId == message.messId }
                            if (index != -1) {
                                arrayItem[index] = message
                            }
                        }
                    }
                    // Refresh the filtered list and update the adapter
                    filterMessages(binding?.edtFind?.text.toString())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // import class usageTracker to count using time
        usageTracker = UsageTracker(this)

        // Prevent dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Inflate the layout using view binding
        binding = ActivityNoteFunctionAcitivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set toolbar
        setSupportActionBar(binding?.toolbar?.root)
        binding?.toolbar?.title?.text = "Quản lý ghi chú"

        // Set up RecyclerView layout and adapter
        binding?.RecycleView?.layoutManager = LinearLayoutManager(this)
//        binding.RecycleView.addItemDecoration(
//            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
//        )

        // Initialize the adapter with an empty list
        myAdapter = MyAdapter(this, arrayListOf())

        // Set up the adapter's callbacks
        myAdapter.onItemClick = { message, position ->
            val intent = Intent(this, NoteFunctionAdjustActivity::class.java)
            intent.putExtra("Message", message)
            startCheckType.launch(intent)
        }

        myAdapter.onItemDeleted = { message ->
            val index = arrayItem.indexOfFirst { it.messId == message.messId }
            if (index != -1) {
                arrayItem.removeAt(index)
            }
        }

        // Set the adapter to the RecyclerView
        binding?.RecycleView?.adapter = myAdapter

        // Firebase Firestore retrieval
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in to access notes.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        db.collection("notes")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                arrayItem.clear()
                for (document in result) {
                    val message = document.toObject(Message::class.java)
                    message.messId = document.id // Set the messId to the document ID
                    arrayItem.add(message)
                }
                // Update the adapter's data
                filterMessages(binding?.edtFind?.text.toString())
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreError", "Error getting documents.", exception)
            }

        // Back Arrow Handling
        binding?.toolbar?.BackArrow?.setOnClickListener {
            onBackPressed()
        }

        // Handle AddMessage button click
        binding?.toolbar?.AddMessage?.setOnClickListener {
            val intent = Intent(this, NoteFunctionAddActivity::class.java)
            startCheckType.launch(intent)
        }

        // Add search functionality to the EditText field
        binding?.edtFind?.addTextChangedListener(object : TextWatcher {
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
        val filteredList = if (searchQuery.isEmpty()) {
            arrayItem.toList() // Create a copy of the full list
        } else {
            arrayItem.filter { message ->
                message.messTitle.lowercase().contains(searchQuery)
            }
        }
        // Update the adapter's data
        myAdapter.setItems(filteredList)
    }

    override fun onResume() {
        super.onResume()
        // Lưu thời gian bắt đầu (mốc thời gian hiện tại) để tính thời gian sử dụng khi Activity bị tạm dừng
        startTime = System.currentTimeMillis()

    }


    override fun onPause() {
        super.onPause()

        // Tính toán thời gian sử dụng Ghi chú
        val endTime = System.currentTimeMillis()
        val durationInMillis = endTime - startTime
        val durationInSeconds = (durationInMillis / 1000).toInt() // Chuyển đổi thời gian từ milliseconds sang giây

        // Kiểm tra nếu thời gian sử dụng hợp lệ (lớn hơn 0 giây) thì lưu vào UsageTracker
        if (durationInSeconds > 0) {
            usageTracker.addUsageTime("Ghi chú", durationInSeconds)
        } else {
            usageTracker.addUsageTime("Ghi chú", 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}



