package com.example.nlcs.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityMainBinding
import com.example.nlcs.preferen.UserSharePreferences

class FlashcardActivity : AppCompatActivity() {
    private var navController: NavController? = null
    private var binding: ActivityMainBinding? = null

    var userSharePreferences: UserSharePreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        setupNavigation()
    }

    private fun setupNavigation() {
        userSharePreferences = UserSharePreferences(this@FlashcardActivity)

        navController = findNavController(this, R.id.nav_host_fragment_activity_main)

        val navGraphId = R.navigation.main_nav
        val menuId = R.menu.menu_nav


        navController!!.setGraph(navGraphId)
        binding.bottomNavigationView.getMenu().clear()
        binding.bottomNavigationView.inflateMenu(menuId)

        setupWithNavController(binding.bottomNavigationView, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }
}
