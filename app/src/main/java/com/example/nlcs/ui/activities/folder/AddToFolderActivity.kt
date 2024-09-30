package com.example.nlcs.ui.activities.folder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nlcs.R
import com.example.nlcs.adapter.folder.FolderSelectAdapter
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.databinding.ActivityAddToFolderBinding
import com.example.nlcs.ui.activities.create.CreateFolderActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddToFolderActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddToFolderBinding.inflate(layoutInflater) }
    private val folderDAO by lazy { FolderDAO(this) }
    private lateinit var adapter: FolderSelectAdapter
    private lateinit var firebaseAuth: FirebaseAuth


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            setContentView(binding.root)
            setupToolbar()
        CoroutineScope(Dispatchers.Main).launch {
            setupRecyclerView()
            setupCreateNewFolder()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private suspend fun setupRecyclerView() {
        // TODO: get all folder
        firebaseAuth = Firebase.auth
        val userId = firebaseAuth.currentUser?.uid ?: ""
        val folders = folderDAO.getAllFolderByUserId(userId)
        adapter = FolderSelectAdapter(folders, intent.getStringExtra("flashcard_id")!!)
        val linearLayoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.folderRv.layoutManager = linearLayoutManager
        binding.folderRv.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun setupCreateNewFolder() {
        binding.createNewFolderTv.setOnClickListener {
            startActivity(Intent(this, CreateFolderActivity::class.java))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tick, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.done) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            setupRecyclerView()
        }
    }
}