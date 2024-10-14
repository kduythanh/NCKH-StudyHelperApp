package com.example.nlcs

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nlcs.NoteFunction.NoteFunctionActivity
import com.example.nlcs.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.nlcs.ui.activities.FlashcardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize Firebase Auth
        firebaseAuth = Firebase.auth

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
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
        updateNavHeader()


        binding.card1.setOnClickListener{
            val intent = Intent(this, FlashcardActivity::class.java)
            startActivity(intent)
        }
       
        // Set click listener for the Mind map
        binding.card2.setOnClickListener {
            val intent = Intent(this, MindMapMenuActivity::class.java)
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
            val intent = Intent(this, NoteFunctionActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for the Reminder
        binding.card6.setOnClickListener {
            val intent = Intent(this, ReminderMenuActivityAPI::class.java)
            startActivity(intent)
        }


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId){
                R.id.nav_home -> {
                    if (!isCurrentActivity(MainActivity::class.java)) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
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
                    binding.navigationView.menu.findItem(R.id.nav_logout).isChecked = false
                    showLogoutConfirmationDialog()
                    true
                }
                R.id.nav_changePassword -> {
                    // Đóng navigation drawer
                    drawerLayout.closeDrawer(GravityCompat.START)
                    // Xóa trạng thái đã chọn của menu item
                    binding.navigationView.menu.findItem(R.id.nav_changePassword).isChecked = false
                    // Chuyển đến activity đổi mật khẩu
                    val intent = Intent(this, ChangePasswordActivity::class.java)
                    startActivity(intent)
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
    // Hàm hiển thị hộp thoại xác nhận
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác nhận đăng xuất")
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")

        builder.setPositiveButton("Có") { _: DialogInterface, _: Int ->
            // Thực hiện đăng xuất
            firebaseAuth.signOut()
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }

        builder.setNegativeButton("Không") { dialog: DialogInterface, _: Int ->
            dialog.dismiss() // Đóng hộp thoại
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun updateNavHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val emailTextView: TextView = headerView.findViewById(R.id.nav_header_email)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            emailTextView.text = currentUser.email // Set the email in the TextView
        }
    }
}
