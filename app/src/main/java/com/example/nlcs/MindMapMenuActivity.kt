package com.example.nlcs

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.databinding.ActivityMindMapMenuBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID


class MindMapMenuActivity : AppCompatActivity(), MindMapListener {

    private lateinit var binding: ActivityMindMapMenuBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var mindMapList: ArrayList<MindMap>
    private lateinit var mindMapAdapter: MindMapAdapter
    private val neo4jUri = "bolt+s://f4454805.databases.neo4j.io"
    private val neo4jUser = "neo4j"
    private val neo4jPassword = "T79xAI8tRj6QzvCfiqDMBAlxb4pabJ1UBh_H7qIqlaQ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindMapMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initializing some variables
        firebaseAuth = Firebase.auth
        setSupportActionBar(binding.toolbar)
        recyclerView = binding.mindMapMenuRecycleView
        mindMapList = arrayListOf()

        // Enable the back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Initialize the adapter
        mindMapAdapter = MindMapAdapter(mindMapList, this, this)

        // Setup RecyclerView and Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mindMapAdapter



        // Fetch the mind maps from Firestore
        fetchMindMaps()

        // Open the dialog that adds a new mind map when the button is clicked
        binding.mindMapAddButton.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_mind_map, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // Setting the click listeners for the confirm button
            dialogView.findViewById<Button>(R.id.dialogAddButtonConfirm).setOnClickListener {
                val title = dialogView.findViewById<EditText>(R.id.dialogAddMindMapItemEditText).text.toString()
                if(title.isNotEmpty()){
                    createMindMapInFireBase(title)
                    dialog.dismiss()
                }else{
                    Toast.makeText(this, "Xin hãy nhập tên cho mind map!", Toast.LENGTH_SHORT).show()
                }
            }

            // Setting the click listeners for the cancel button
            dialogView.findViewById<Button>(R.id.dialogAddButtonCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    // Create a new mind map in Firestore
    private fun createMindMapInFireBase(title: String){
        val db = FirebaseFirestore.getInstance()
        val mindMapId = UUID.randomUUID().toString()
        val mindMap = hashMapOf(
            "title" to title,
            "mindMapID" to mindMapId,
            "date" to System.currentTimeMillis(),
            "userID" to firebaseAuth.currentUser?.uid
        )

        db.collection("mindMapTemp")
            .add(mindMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Tạo mind map thành công!" , Toast.LENGTH_SHORT).show()
                // Create a Master Node upon creating a new Mind Map item
                val userId = firebaseAuth.currentUser?.uid
                if(userId != null){

                    val y = binding.mindMapMenuRecycleView.width.toFloat() / 2f - binding.toolbar.height.toFloat() - 45f
                    val x = binding.mindMapMenuRecycleView.height.toFloat() / 2f

                    val neo4jService = Neo4jService(neo4jUri, neo4jUser, neo4jPassword)
                    neo4jService.createNode("Nút chính", userId, mindMapId, x , y)
                    neo4jService.close()
                }

                fetchMindMaps()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Tạo mind map thất bại!", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchMindMaps() {
        val db = FirebaseFirestore.getInstance()
        val currentUserID = firebaseAuth.currentUser?.uid
        val docRef = db.collection("mindMapTemp").whereEqualTo("userID", currentUserID)

        docRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshots != null) {
                val newMindMapList = arrayListOf<MindMap>()
                for (doc in snapshots) {
                    val mindMap = doc.toObject(MindMap::class.java).apply {
                        id = doc.id
                    }
                    newMindMapList.add(mindMap)
                }

                // Clear the list and add the new items
                mindMapList.clear()
                mindMapList.addAll(newMindMapList)

                // Sort items by date in ascending order
                mindMapList.sortBy { it.date }

                // Notify the adapter of the changes
                mindMapAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onMindMapDeleted() {
        fetchMindMaps()
    }
}