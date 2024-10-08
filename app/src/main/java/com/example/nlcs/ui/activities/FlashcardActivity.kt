package com.example.nlcs.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI

import com.example.nlcs.R
import com.example.nlcs.databinding.ActivityFlashcardBinding
import com.example.nlcs.preferen.UserSharePreferences

class FlashcardActivity : AppCompatActivity() {
    private var navController: NavController? = null
    private var binding: ActivityFlashcardBinding? = null

    var userSharePreferences: UserSharePreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        setupNavigation()
    }

    private fun setupNavigation() {
        userSharePreferences = UserSharePreferences(this)

        // Initialize the navController
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)

        // Define the navGraphId and menuId
        val navGraphId = R.navigation.main_nav
        val menuId = R.menu.menu_nav

        // Set the navigation graph
        navController.setGraph(navGraphId)

        // Clear and inflate the bottom navigation menu
        binding?.bottomNavigationView?.menu?.clear()
        binding?.bottomNavigationView?.inflateMenu(menuId)

        // Set up the navigation controller with the bottom navigation view
        binding?.let { NavigationUI.setupWithNavController(it.bottomNavigationView, navController) }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }
}
