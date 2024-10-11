package com.example.nlcs.ui.fragments.library

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nlcs.R
import com.example.nlcs.adapter.flashcard.SetCopyAdapter
import com.example.nlcs.data.dao.FlashCardDAO
import com.example.nlcs.data.model.FlashCard
import com.example.nlcs.databinding.FragmentStudySetsBinding
import com.example.nlcs.ui.activities.create.CreateSetActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudySetsFragment : Fragment() {
    private var _binding: FragmentStudySetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private var flashCards: ArrayList<FlashCard> = ArrayList()
    private lateinit var flashCardDAO: FlashCardDAO
    private var setsAdapter: SetCopyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flashCardDAO = FlashCardDAO(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudySetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        binding.createSetBtn.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(), CreateSetActivity::class.java
                )
            )
        }

        firebaseAuth = Firebase.auth
        val userId = firebaseAuth.currentUser?.uid ?: ""

        // Launching a coroutine to call the suspend function
        lifecycleScope.launch {
            refreshData(userId) // Pass userId to refreshData
        }
    }

    private suspend fun refreshData(userId: String) {
        // Clear the current flashCards
        flashCards.clear()

        // Fetch new data
        val newCards = flashCardDAO.getAllFlashCardByUserId(userId)
        if (newCards != null) {
            flashCards.addAll(newCards) // Add new data
        }

        updateVisibility() // Update visibility based on new data
        setupRecyclerView() // Setup the RecyclerView with updated data
    }

    private fun updateVisibility() {
        if (flashCards.isEmpty()) {
            binding.setsCl.visibility = View.VISIBLE
            binding.setsRv.visibility = View.GONE
        } else {
            binding.setsCl.visibility = View.GONE
            binding.setsRv.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        if (setsAdapter == null) { // Check if the adapter is already created
            setsAdapter = SetCopyAdapter(requireContext(), flashCards)
            binding.setsRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            binding.setsRv.adapter = setsAdapter
        } else {
            setsAdapter?.notifyDataSetChanged() // Notify changes if adapter already exists
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}

