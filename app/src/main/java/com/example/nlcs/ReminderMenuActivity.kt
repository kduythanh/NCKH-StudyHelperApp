package com.example.nlcs

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.nlcs.databinding.ActivityReminderMenuBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
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
        firebaseAuth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)
        recyclerView = binding.reminderMenuRecycleView
        reminderList = arrayListOf()
        // Initialize the adapter
        reminderAdapter = ReminderAdapter(reminderList, this)
        // Setup RecyclerView and Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reminderAdapter
        // Open the dialog that adds a new reminder when the button is clicked
        binding.reminderAddButton.setOnClickListener {
            showAddReminderDialog()
        }
        // Initialize the swipe refresh layout
        swipeRefreshLayout = binding.reminderMenuSwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            fetchReminders()
        }
        // Fetch the mind maps from Firestore
        fetchReminders()
    }
    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val reminderName = dialogView.findViewById<EditText>(R.id.dialogAddReminderItemEditText)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.time_picker)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.date_picker)
//        val switchReminder = dialogView.findViewById<SwitchCompat>(R.id.switchReminder)

        dialogView.findViewById<Button>(R.id.dialogAddButtonConfirm).setOnClickListener {
            val title = reminderName.text.toString()
            val hour = timePicker.hour
            val minute = timePicker.minute
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1
            val year = datePicker.year
            val userId = firebaseAuth.currentUser?.uid ?: "unknown_user"

            if (title.isNotEmpty()) {
                val calendar = Calendar.getInstance().apply {
                    set(year, month - 1, day, hour, minute)
                }
                val reminder = Reminder(
                    id = UUID.randomUUID().toString(),
                    user = userId,
                    name = title,
                    hour = hour,
                    minute = minute,
                    date = calendar.time,
                    isActivated = true
                )
                saveReminderToFirestore(reminder)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.dialogAddButtonCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveReminderToFirestore(reminder: Reminder) {
        val db = FirebaseFirestore.getInstance()
        db.collection("reminderTemp").add(reminder)
            .addOnSuccessListener {
                Toast.makeText(this, "Reminder added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add reminder", Toast.LENGTH_SHORT).show()
            }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchReminders() {
        val db = FirebaseFirestore.getInstance()
        val userId = firebaseAuth.currentUser?.uid ?: "unknown_user"

        db.collection("reminderTemp")
            .whereEqualTo("user", userId) // Lọc reminders theo người dùng hiện tại
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val newReminderList = arrayListOf<Reminder>()
                    for (doc in snapshots.documents) {
                        val reminder = doc.toObject(Reminder::class.java)
                        reminder?.let {
                            it.id = doc.id
                            newReminderList.add(it)
                        }
                    }
                    reminderList.clear()
                    reminderList.addAll(newReminderList)
                    reminderList.sortBy { it.date }
                    reminderAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
    }

//        // Initialize the drawer layout
//        drawerLayout = binding.drawerLayout
//        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()



//        // Open the drawer when the menu button is clicked
//        binding.toolbar.setNavigationOnClickListener{
//            if(drawerLayout.isDrawerOpen(binding.navigationView)){
//                drawerLayout.closeDrawer(binding.navigationView)
//            }else{
//                drawerLayout.openDrawer(binding.navigationView)
//            }
//        }
                // Setting the click listeners for the cancel button
//            dialogView.findViewById<Button>(R.id.dialogAddButtonCancel).setOnClickListener {
//                dialog.dismiss()
//            }
//
//            dialog.show()
            // Fetch the mind maps from Firestore

        // Setting the navigation listener
//        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
//            when (menuItem.itemId){
//                R.id.nav_home -> {
//                    finish()
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                    true
//                }
//                R.id.nav_user_profile -> {
//                    if (!isCurrentActivity(MainActivity::class.java)) {
//                        startActivity(Intent(this, MainActivity::class.java))
//                    }
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                    true
//                }
//                R.id.nav_setting -> {
//                    if (!isCurrentActivity(MainActivity::class.java)) {
//                        startActivity(Intent(this, MainActivity::class.java))
//                    }
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                    true
//                }
//                R.id.nav_logout -> {
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                    firebaseAuth.signOut()
//                    val intent = Intent(this, LogInActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
//                    finish()
//                    true
//                }
//
//                else -> false
//            }
//        }

        // On back pressed listener for the drawer
//        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
//            override fun handleOnBackPressed(){
//                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                }else{
//                    finish()
//                }
//            }
//        })
}
    // Check if the current activity is the same as the given class
//    private fun isCurrentActivity(activityClass: Class<*>): Boolean {
//        return activityClass == this::class.java
//    }


