package com.example.nlcs.ui.activities.set

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.R
import com.example.nlcs.adapter.flashcard.SetFolderViewAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.ActivityAddFlashCardBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFlashCardActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityAddFlashCardBinding.inflate(layoutInflater)
    }
    private lateinit var flashCardDAO: FlashCardDAO
    private lateinit var flashCardList: ArrayList<FlashCard>
    private lateinit var adapter: SetFolderViewAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    //private lateinit var userSharePreferences: UserSharePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        // Start a coroutine to fetch flashcards asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            // Get the current user ID
            firebaseAuth = Firebase.auth
            val userId = firebaseAuth.currentUser?.uid ?: ""

            // Initialize the DAO
            flashCardDAO = FlashCardDAO(this@AddFlashCardActivity) // Replace with actual activity or context

            // Fetch the flashcards asynchronously
            val flashCardList = flashCardDAO.getAllFlashCardByUserId(userId)

            // Set up the RecyclerView with the fetched flashcards
            adapter = SetFolderViewAdapter(flashCardList, true, intent.getStringExtra("id_folder")!!)
            val linearLayoutManager = LinearLayoutManager(this@AddFlashCardActivity, LinearLayoutManager.VERTICAL, false)
            binding.flashcardRv.layoutManager = linearLayoutManager
            binding.flashcardRv.adapter = adapter

            // Notify adapter that data has changed
            adapter.notifyDataSetChanged()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tick, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.done -> {
                onBackPressedDispatcher.onBackPressed()
                Toast.makeText(this, "Added to folder", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }


}