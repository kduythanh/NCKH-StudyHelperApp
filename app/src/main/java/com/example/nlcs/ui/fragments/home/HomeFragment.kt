package com.example.nlcs.ui.fragments.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.ui.activities.search.ViewSearchActivity
import com.example.nlcs.adapter.flashcard.SetsAdapter
import com.example.nlcs.adapter.folder.FolderAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.dao.FolderDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.data.model.Folder
import com.example.nlcs.databinding.FragmentHomeBinding
import com.example.nlcs.ui.activities.create.CreateSetActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var flashCards: ArrayList<FlashCard>? = null
    private var folders: ArrayList<Folder>? = null
    private var flashCardDAO: FlashCardDAO? = null
    private var folderDAO: FolderDAO? = null
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity()
        flashCardDAO = FlashCardDAO(requireActivity())
        folderDAO = FolderDAO(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            requireActivity()

            setupFlashCards()
            setupFolders()
            setupVisibility()
            setupSwipeRefreshLayout()
            setupSearchBar()
            setupCreateSetsButton()
        }


        binding!!.swipeRefreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                refreshData()
            }
            binding!!.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireActivity(), "Refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun setupFlashCards() {
        firebaseAuth = Firebase.auth
        val userId = firebaseAuth.currentUser?.uid ?: ""
        flashCards = flashCardDAO!!.getAllFlashCardByUserId(userId )
        val linearLayoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        binding!!.setsRv.layoutManager = linearLayoutManager
        val setsAdapter = SetsAdapter(requireActivity(), flashCards!!, false)
        binding!!.setsRv.adapter = setsAdapter
        setsAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun setupFolders() {
        firebaseAuth = Firebase.auth
        val userId = firebaseAuth.currentUser?.uid ?: ""
        folders = folderDAO!!.getAllFolderByUserId(userId)
        val folderAdapter = FolderAdapter(requireActivity(), folders!!)
        val linearLayoutManager1 =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        binding!!.foldersRv.layoutManager = linearLayoutManager1
        binding!!.foldersRv.adapter = folderAdapter
        folderAdapter.notifyDataSetChanged()
    }


    private fun setupVisibility() {
        if (flashCards!!.isEmpty()) {
            binding!!.setsCl.visibility = View.GONE
        } else {
            binding!!.setsCl.visibility = View.VISIBLE
        }
        if (folders!!.isEmpty()) {
            binding!!.folderCl.visibility = View.GONE
        } else {
            binding!!.folderCl.visibility = View.VISIBLE
        }
    }

    private fun setupSwipeRefreshLayout() {
        binding!!.swipeRefreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                refreshData()
            }
            binding!!.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupSearchBar() {
        binding!!.searchBar.setOnClickListener { v: View? ->
            val intent = Intent(requireActivity(), ViewSearchActivity::class.java)
           startActivity(intent)
       }
   }

    private fun setupCreateSetsButton() {
        binding!!.createSetsCl.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    activity, CreateSetActivity::class.java
                )
            )
        }
    }

    private suspend fun refreshData() {
        setupFlashCards()
        setupFolders()
        setupVisibility()
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            refreshData()

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
