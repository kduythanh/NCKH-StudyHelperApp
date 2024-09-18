package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nlcs.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout


    // sửa code tại đây
    private lateinit var usageTracker: UsageTracker
    private var startTime: Long = 0

    // end here
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        setSupportActionBar(binding.toolbar)

        drawerLayout = binding.drawerLayout
        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.toolbar.setNavigationOnClickListener {
            if (drawerLayout.isDrawerOpen(binding.navigationView)) {
                drawerLayout.closeDrawer(binding.navigationView)
            } else {
                drawerLayout.openDrawer(binding.navigationView)
            }
        }

        // Set click listener for the Flash card
        binding.card1.setOnClickListener {
            val intent = Intent(this, FlashCardActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for the Mind map
        binding.card2.setOnClickListener {
            val intent = Intent(this, MindMapActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for the Focus card
        binding.card3.setOnClickListener {
            val intent = Intent(this, FocusActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for the Statistics
        binding.card4.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for the Focus card
        binding.card5.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            startActivity(intent)
        }


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }
}

