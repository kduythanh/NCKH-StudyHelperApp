package com.example.nlcs

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.nlcs.databinding.ActivityReminderMenuBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID

class ReminderMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderMenuBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var reminderList: ArrayList<Reminder>
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializing some variables
        firebaseAuth = Firebase.auth
        setSupportActionBar(binding.toolbar)
        recyclerView = binding.mindMapMenuRecycleView
        reminderList = arrayListOf()

        // Initialize the adapter
        reminderAdapter = ReminderAdapter(reminderList, this)

//        // Initialize the drawer layout
//        drawerLayout = binding.drawerLayout
//        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()

        // Setup RecyclerView and Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reminderAdapter

//        // Initialize the swipe refresh layout
//        swipeRefreshLayout = binding.reminderMenuSwipeRefreshLayout
//        swipeRefreshLayout.setOnRefreshListener {
//            fetchMindMaps()
//        }

//        // Open the drawer when the menu button is clicked
//        binding.toolbar.setNavigationOnClickListener{
//            if(drawerLayout.isDrawerOpen(binding.navigationView)){
//                drawerLayout.closeDrawer(binding.navigationView)
//            }else{
//                drawerLayout.openDrawer(binding.navigationView)
//            }
//        }

        // Fetch the mind maps from Firestore
        fetchReminders()

        // Open the dialog that adds a new mind map when the button is clicked
        binding.mindMapAddButton.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // Setting the click listeners for the confirm button
            dialogView.findViewById<Button>(R.id.dialogAddButtonConfirm).setOnClickListener {
                val title = dialogView.findViewById<EditText>(R.id.dialogAddReminderItemEditText).text.toString()

                if(title.isNotEmpty()){
                    createReminderInFireBase(id, time, date, name, isActivated)
                    dialog.dismiss()
                }else{
                    Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                }
            }

            // Setting the click listeners for the cancel button
            dialogView.findViewById<Button>(R.id.dialogAddButtonCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }



        // Setting the navigation listener
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId){
                R.id.nav_home -> {
                    finish()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_user_profile -> {
                    if (!isCurrentActivity(MainActivity::class.java)) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_setting -> {
                    if (!isCurrentActivity(MainActivity::class.java)) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    firebaseAuth.signOut()
                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }

                else -> false
            }
        }

        // On back pressed listener for the drawer
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed(){
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START)
                }else{
                    finish()
                }
            }
        })
    }

    // Check if the current activity is the same as the given class
    private fun isCurrentActivity(activityClass: Class<*>): Boolean {
        return activityClass == this::class.java
    }

    // Create a new reminder in Firestore
    private fun createReminderInFireBase(id: String, time: Calendar, date: Calendar, name: String, isActivated: Boolean){
        val db = FirebaseFirestore.getInstance()
        val mindMap = hashMapOf(
            "id" to id,
            "time" to time,
            "date" to date,
            "name" to name,
            "isActivated" to isActivated
        )

        db.collection("reminderTemp")
            .add(mindMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Reminder Created" , Toast.LENGTH_SHORT).show()
                fetchReminders()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create reminder", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch the mind maps from Firestore
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchReminders(){
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("mindMapTemp")

        docRef.addSnapshotListener { snapshots, e ->
            if (e != null){
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if(snapshots != null){
                val newReminderList = arrayListOf<Reminder>()
                for (doc in snapshots){
                    val reminder = doc.toObject(Reminder::class.java).apply{
                        id = doc.id
                    }
                    newReminderList.add(reminder)
                }

                // Calculate the starting position for new items
                val startPosition = reminderList.size

                // Clear the list and add the new items
                reminderList.clear()
                reminderList.addAll(newReminderList)

                // Sort items by date in in ascending order
                reminderList.sortBy { it.date }

                // Notify the adapter of the newly inserted items
                // Refresh the recycler view when adding new items to an empty list
                reminderAdapter.notifyItemRangeInserted(startPosition, newReminderList.size)

                // Notify the adapter that the data has changed
                // This prevent the app from crashing when either updating the title or deleting the the mind map
                reminderAdapter.notifyDataSetChanged( )

                // Stop The refreshing animation
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

}