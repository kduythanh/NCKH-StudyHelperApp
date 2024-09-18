package com.example.nlcs

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
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

        binding.toolbar.setNavigationOnClickListener{
            if(drawerLayout.isDrawerOpen(binding.navigationView)){
                drawerLayout.closeDrawer(binding.navigationView)
            }else{
                drawerLayout.openDrawer(binding.navigationView)
            }
        }
        // Update NavigationView header with user email
        updateNavHeader()

//<<<<<<< Updated upstream
//=======

//        binding.card2.setOnClickListener{
//            val intent = Intent(this, MindMapMenuActivity::class.java)
//            startActivity(intent)
//        }

        binding.card6.setOnClickListener {
            val intent = Intent(this, ReminderMenuActivityAPI::class.java)
            startActivity(intent)
        }

//>>>>>>> Stashed changes
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId){
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    firebaseAuth.signOut()
                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                    finish()
                    true
                }

                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed(){
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START)
                }else{
                    finish()
                }
            }
        })

        // Test Click listener for log out button
//        binding.logOutTestButton.setOnClickListener {
//            firebaseAuth.signOut()
//            val intent = Intent(this, LogInActivity::class.java)
//            startActivity(intent)
//            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
//            finish()
//        }

    }
    private fun updateNavHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val emailTextView: TextView = headerView.findViewById(R.id.nav_header_email)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            emailTextView.text = currentUser.email // Set the email in the TextView
        }
    }
//    private fun isCurrentActivity(activityClass: Class<*>): Boolean {
//        return activityClass == this::class.java
//    }
}